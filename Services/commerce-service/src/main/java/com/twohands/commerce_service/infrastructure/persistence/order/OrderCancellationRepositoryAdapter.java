package com.twohands.commerce_service.infrastructure.persistence.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.application.order.common.InventoryReleasedOutboxService;
import com.twohands.commerce_service.application.order.common.OrderCancelPendingRefundOutboxService;
import com.twohands.commerce_service.application.order.common.OrderCancelledOutboxService;
import com.twohands.commerce_service.domain.inventory.ReserveInventoryRepository;
import com.twohands.commerce_service.domain.order.BuyerOrderCancelOutcome;
import com.twohands.commerce_service.domain.order.BuyerOrderCancellationPolicy;
import com.twohands.commerce_service.domain.order.BuyerOrderCancellationResult;
import com.twohands.commerce_service.domain.order.OrderCancellationRepository;
import com.twohands.commerce_service.domain.order.OrderItemQuantity;
import com.twohands.commerce_service.domain.order.OrderShipmentCancellationGuard;
import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.order.PaymentRefundRequestRepository;
import com.twohands.commerce_service.domain.order.RefundCancellationPolicy;
import com.twohands.commerce_service.domain.order.SellerOrderCancellationPolicy;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentRefundRequestedBy;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class OrderCancellationRepositoryAdapter implements OrderCancellationRepository {

    private static final Logger log = LoggerFactory.getLogger(OrderCancellationRepositoryAdapter.class);
    private static final String DEFAULT_BUYER_REASON = "BUYER_CANCELLED";
    private static final String DEFAULT_SELLER_REASON = "SELLER_CANCELLED";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final OutboxEventRepository outboxEventRepository;
    private final OrderCancelledOutboxService orderCancelledOutboxService;
    private final OrderCancelPendingRefundOutboxService orderCancelPendingRefundOutboxService;
    private final InventoryReleasedOutboxService inventoryReleasedOutboxService;
    private final PaymentRefundRequestRepository paymentRefundRequestRepository;
    private final ReserveInventoryRepository reserveInventoryRepository;
    private final ObjectMapper objectMapper;

    public OrderCancellationRepositoryAdapter(
            NamedParameterJdbcTemplate jdbcTemplate,
            OutboxEventRepository outboxEventRepository,
            OrderCancelledOutboxService orderCancelledOutboxService,
            OrderCancelPendingRefundOutboxService orderCancelPendingRefundOutboxService,
            InventoryReleasedOutboxService inventoryReleasedOutboxService,
            PaymentRefundRequestRepository paymentRefundRequestRepository,
            ReserveInventoryRepository reserveInventoryRepository,
            ObjectMapper objectMapper
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.outboxEventRepository = outboxEventRepository;
        this.orderCancelledOutboxService = orderCancelledOutboxService;
        this.orderCancelPendingRefundOutboxService = orderCancelPendingRefundOutboxService;
        this.inventoryReleasedOutboxService = inventoryReleasedOutboxService;
        this.paymentRefundRequestRepository = paymentRefundRequestRepository;
        this.reserveInventoryRepository = reserveInventoryRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public BuyerOrderCancellationResult cancelByBuyer(UUID orderId, UUID buyerId, String reason, Instant now) {
        OrderRow order = lockOrderForBuyer(orderId, buyerId);
        if (order == null) {
            return new BuyerOrderCancellationResult(BuyerOrderCancelOutcome.NOT_FOUND, orderId, null);
        }
        return cancelOrder(order, buyerId, PaymentRefundRequestedBy.BUYER, reason, now, "BUYER", DEFAULT_BUYER_REASON);
    }

    @Override
    @Transactional
    public BuyerOrderCancellationResult cancelBySeller(UUID orderId, UUID sellerId, String reason, Instant now) {
        if (!sellerOwnsEntireOrder(orderId, sellerId)) {
            return new BuyerOrderCancellationResult(BuyerOrderCancelOutcome.NOT_FOUND, orderId, null);
        }

        OrderRow order = lockOrderById(orderId);
        if (order == null) {
            return new BuyerOrderCancellationResult(BuyerOrderCancelOutcome.NOT_FOUND, orderId, null);
        }

        return cancelOrder(order, sellerId, PaymentRefundRequestedBy.SELLER, reason, now, "SELLER", DEFAULT_SELLER_REASON);
    }

    private BuyerOrderCancellationResult cancelOrder(
            OrderRow order,
            UUID actorId,
            PaymentRefundRequestedBy requestedBy,
            String reason,
            Instant now,
            String changedBy,
            String defaultReason
    ) {
        UUID orderId = order.id();

        if ("CANCELLED".equals(order.status)) {
            return new BuyerOrderCancellationResult(BuyerOrderCancelOutcome.ALREADY_CANCELLED, orderId, now);
        }

        PaymentMethod paymentMethod = PaymentMethod.valueOf(order.paymentMethod);
        PaymentStatus paymentStatus = PaymentStatus.valueOf(order.paymentStatus);
        OrderStatus orderStatus = OrderStatus.valueOf(order.status);

        boolean buyerFlow = requestedBy == PaymentRefundRequestedBy.BUYER;
        boolean allowed = buyerFlow
                ? BuyerOrderCancellationPolicy.canCancelByBuyer(orderStatus, paymentStatus, paymentMethod)
                : SellerOrderCancellationPolicy.canCancelBySeller(orderStatus, paymentStatus, paymentMethod);
        if (!allowed) {
            return new BuyerOrderCancellationResult(BuyerOrderCancelOutcome.NOT_CANCELLABLE, orderId, null);
        }

        var activeRefund = paymentRefundRequestRepository.findActiveByOrderId(orderId);
        if (activeRefund.isPresent()) {
            return new BuyerOrderCancellationResult(
                    BuyerOrderCancelOutcome.REFUND_ALREADY_REQUESTED,
                    orderId,
                    activeRefund.get().requestedAt(),
                    activeRefund.get().refundRequestId()
            );
        }

        if (hasShipmentBlockingCancel(orderId)) {
            return new BuyerOrderCancellationResult(BuyerOrderCancelOutcome.NOT_CANCELLABLE, orderId, null);
        }

        if (RefundCancellationPolicy.routesToRefundQueue(paymentMethod, paymentStatus, orderStatus)) {
            return queueRefundRequest(order, actorId, requestedBy, reason, now, defaultReason);
        }

        return cancelImmediately(order, actorId, reason, now, changedBy, defaultReason);
    }

    private BuyerOrderCancellationResult queueRefundRequest(
            OrderRow order,
            UUID actorId,
            PaymentRefundRequestedBy requestedBy,
            String reason,
            Instant now,
            String defaultReason
    ) {
        PaymentRow payment = lockPaymentByOrderId(order.id());
        if (payment == null || !PaymentStatus.PAID.name().equals(payment.status)) {
            return new BuyerOrderCancellationResult(BuyerOrderCancelOutcome.NOT_CANCELLABLE, order.id(), null);
        }

        String cancelReason = resolveReason(reason, defaultReason);
        UUID refundRequestId = paymentRefundRequestRepository.createRequested(
                payment.id,
                order.id(),
                requestedBy,
                actorId,
                order.finalAmount(),
                cancelReason,
                now
        );

        outboxEventRepository.save(orderCancelPendingRefundOutboxService.build(
                refundRequestId,
                order.id(),
                payment.id,
                order.buyerId(),
                findDistinctSellerIds(order.id()),
                requestedBy,
                actorId,
                order.finalAmount(),
                cancelReason,
                now
        ));

        return new BuyerOrderCancellationResult(
                BuyerOrderCancelOutcome.PENDING_REFUND,
                order.id(),
                now,
                refundRequestId
        );
    }

    private BuyerOrderCancellationResult cancelImmediately(
            OrderRow order,
            UUID actorUserId,
            String reason,
            Instant now,
            String changedBy,
            String defaultReason
    ) {
        UUID orderId = order.id();
        PaymentRow payment = lockPaymentByOrderId(orderId);
        if (payment == null || !PaymentStatus.PENDING.name().equals(payment.status)) {
            return new BuyerOrderCancellationResult(BuyerOrderCancelOutcome.NOT_CANCELLABLE, orderId, null);
        }

        String cancelReason = resolveReason(reason, defaultReason);
        List<OrderItemQuantity> releasableItems = findReleasableOrderItems(orderId);
        releaseInventoryOrThrow(releasableItems, orderId, now);
        syncProductStatusesAfterRelease(releasableItems, now);

        int paymentUpdated = cancelPayment(payment.id, now);
        if (paymentUpdated == 0) {
            return new BuyerOrderCancellationResult(BuyerOrderCancelOutcome.NOT_CANCELLABLE, orderId, null);
        }

        int orderUpdated = cancelOrder(orderId, now);
        if (orderUpdated == 0) {
            return new BuyerOrderCancellationResult(BuyerOrderCancelOutcome.NOT_CANCELLABLE, orderId, null);
        }

        cancelReleasableOrderItems(orderId, now);
        insertOrderStatusHistory(orderId, order.status, cancelReason, now, changedBy);
        insertPaymentStatusHistory(payment.id, payment.status, cancelReason, now, changedBy);

        outboxEventRepository.save(
                orderCancelledOutboxService.build(
                        orderId,
                        order.buyerId(),
                        findDistinctSellerIds(orderId),
                        cancelReason,
                        changedBy,
                        actorUserId,
                        now
                )
        );
        if (!releasableItems.isEmpty()) {
            outboxEventRepository.save(inventoryReleasedOutboxService.build(orderId, releasableItems, now));
        }

        return new BuyerOrderCancellationResult(BuyerOrderCancelOutcome.CANCELLED, orderId, now);
    }

    private List<UUID> findDistinctSellerIds(UUID orderId) {
        String sql = """
                SELECT DISTINCT seller_id
                FROM order_items
                WHERE order_id = :orderId
                ORDER BY seller_id
                """;
        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("orderId", orderId),
                (rs, rowNum) -> UUID.fromString(rs.getString("seller_id"))
        );
    }

    private boolean sellerOwnsEntireOrder(UUID orderId, UUID sellerId) {
        String sql = """
                SELECT COUNT(*) AS item_count,
                       COUNT(*) FILTER (WHERE seller_id = :sellerId) AS owned_count,
                       COUNT(DISTINCT seller_id) AS seller_count
                FROM order_items
                WHERE order_id = :orderId
                """;
        Map<String, Object> row = jdbcTemplate.queryForMap(sql, new MapSqlParameterSource()
                .addValue("orderId", orderId)
                .addValue("sellerId", sellerId));
        long itemCount = ((Number) row.get("item_count")).longValue();
        long ownedCount = ((Number) row.get("owned_count")).longValue();
        long sellerCount = ((Number) row.get("seller_count")).longValue();
        return itemCount > 0 && ownedCount == itemCount && sellerCount == 1;
    }

    private OrderRow lockOrderForBuyer(UUID orderId, UUID buyerId) {
        String sql = """
                SELECT id, buyer_id, status, payment_status, payment_method, final_amount
                FROM orders
                WHERE id = :orderId AND buyer_id = :buyerId
                FOR UPDATE
                """;
        List<OrderRow> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("orderId", orderId)
                        .addValue("buyerId", buyerId),
                (rs, rowNum) -> mapOrderRow(rs)
        );
        return rows.isEmpty() ? null : rows.getFirst();
    }

    private OrderRow lockOrderById(UUID orderId) {
        String sql = """
                SELECT id, buyer_id, status, payment_status, payment_method, final_amount
                FROM orders
                WHERE id = :orderId
                FOR UPDATE
                """;
        List<OrderRow> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("orderId", orderId),
                (rs, rowNum) -> mapOrderRow(rs)
        );
        return rows.isEmpty() ? null : rows.getFirst();
    }

    private PaymentRow lockPaymentByOrderId(UUID orderId) {
        String sql = """
                SELECT id, order_id, status, amount
                FROM payments
                WHERE order_id = :orderId
                FOR UPDATE
                """;
        List<PaymentRow> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("orderId", orderId),
                (rs, rowNum) -> mapPaymentRow(rs)
        );
        return rows.isEmpty() ? null : rows.getFirst();
    }

    private boolean hasShipmentBlockingCancel(UUID orderId) {
        String sql = """
                SELECT status::text AS status
                FROM shipments
                WHERE order_id = :orderId
                """;
        List<String> statuses = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("orderId", orderId),
                (rs, rowNum) -> rs.getString("status")
        );
        return statuses.stream().anyMatch(OrderShipmentCancellationGuard::isBlockingShipmentStatus);
    }

    private int cancelPayment(UUID paymentId, Instant now) {
        String sql = """
                UPDATE payments
                SET status = 'CANCELLED', updated_at = :now
                WHERE id = :paymentId AND status = 'PENDING'
                """;
        return jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("paymentId", paymentId)
                .addValue("now", Timestamp.from(now)));
    }

    private int cancelOrder(UUID orderId, Instant now) {
        String sql = """
                UPDATE orders
                SET status = 'CANCELLED',
                    payment_status = 'CANCELLED',
                    updated_at = :now
                WHERE id = :orderId
                  AND payment_status = 'PENDING'
                  AND (
                      status IN ('CREATED', 'AWAITING_PAYMENT')
                      OR (status = 'PROCESSING' AND payment_method = 'COD')
                  )
                """;
        return jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("orderId", orderId)
                .addValue("now", Timestamp.from(now)));
    }

    private void cancelReleasableOrderItems(UUID orderId, Instant now) {
        String sql = """
                UPDATE order_items
                SET status = 'CANCELLED', updated_at = :now
                WHERE order_id = :orderId AND status IN ('PENDING', 'PROCESSING')
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("orderId", orderId)
                .addValue("now", Timestamp.from(now)));
    }

    private List<OrderItemQuantity> findReleasableOrderItems(UUID orderId) {
        String sql = """
                SELECT id, product_id, quantity
                FROM order_items
                WHERE order_id = :orderId AND status IN ('PENDING', 'PROCESSING')
                """;
        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("orderId", orderId),
                (rs, rowNum) -> new OrderItemQuantity(
                        UUID.fromString(rs.getString("id")),
                        UUID.fromString(rs.getString("product_id")),
                        rs.getInt("quantity")
                )
        );
    }

    private void releaseInventoryOrThrow(List<OrderItemQuantity> items, UUID orderId, Instant now) {
        String sql = """
                UPDATE product_inventories
                SET reserved_quantity = reserved_quantity - :quantity,
                    stock_quantity = stock_quantity + :quantity,
                    updated_at = :now
                WHERE product_id = :productId
                  AND reserved_quantity >= :quantity
                """;
        for (OrderItemQuantity item : items) {
            int updated = jdbcTemplate.update(sql, new MapSqlParameterSource()
                    .addValue("productId", item.productId())
                    .addValue("quantity", item.quantity())
                    .addValue("now", Timestamp.from(now)));
            if (updated == 0) {
                log.error(
                        "Failed to release inventory for order {} product {} quantity {}",
                        orderId,
                        item.productId(),
                        item.quantity()
                );
                throw new AppException(
                        ErrorCode.INTERNAL_ERROR,
                        "Inventory release conflict for order " + orderId
                );
            }
        }
    }

    private void syncProductStatusesAfterRelease(List<OrderItemQuantity> items, Instant now) {
        if (items.isEmpty()) {
            return;
        }
        List<UUID> productIds = items.stream()
                .map(OrderItemQuantity::productId)
                .distinct()
                .toList();
        reserveInventoryRepository.syncInStockProductStatuses(productIds, now);
    }

    private void insertOrderStatusHistory(
            UUID orderId,
            String oldStatus,
            String reason,
            Instant now,
            String changedBy
    ) {
        String sql = """
                INSERT INTO order_status_history(id, order_id, old_status, new_status, changed_by, note, created_at)
                VALUES (:id, :orderId, CAST(:oldStatus AS order_status), 'CANCELLED', :changedBy, :note, :createdAt)
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", UUID.randomUUID())
                .addValue("orderId", orderId)
                .addValue("oldStatus", oldStatus)
                .addValue("changedBy", changedBy)
                .addValue("note", reason)
                .addValue("createdAt", Timestamp.from(now)));
    }

    private void insertPaymentStatusHistory(
            UUID paymentId,
            String oldStatus,
            String reason,
            Instant now,
            String changedBy
    ) {
        String sql = """
                INSERT INTO payment_status_history(id, payment_id, old_status, new_status, payload, created_at)
                VALUES (:id, :paymentId, CAST(:oldStatus AS payment_status), 'CANCELLED', CAST(:payload AS jsonb), :createdAt)
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", UUID.randomUUID())
                .addValue("paymentId", paymentId)
                .addValue("oldStatus", oldStatus)
                .addValue("payload", buildPaymentHistoryPayload(reason, now, changedBy))
                .addValue("createdAt", Timestamp.from(now)));
    }

    private String buildPaymentHistoryPayload(String reason, Instant now, String changedBy) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("reason", reason);
        payload.put("cancelled_by", changedBy);
        payload.put("processed_at", now.toString());
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Cannot serialize payment history payload", ex);
        }
    }

    private String resolveReason(String reason, String defaultReason) {
        if (reason == null || reason.isBlank()) {
            return defaultReason;
        }
        return reason.trim();
    }

    private OrderRow mapOrderRow(ResultSet rs) throws SQLException {
        return new OrderRow(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("buyer_id")),
                rs.getString("status"),
                rs.getString("payment_status"),
                rs.getString("payment_method"),
                rs.getBigDecimal("final_amount")
        );
    }

    private PaymentRow mapPaymentRow(ResultSet rs) throws SQLException {
        return new PaymentRow(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("order_id")),
                rs.getString("status")
        );
    }

    private record OrderRow(
            UUID id,
            UUID buyerId,
            String status,
            String paymentStatus,
            String paymentMethod,
            BigDecimal finalAmount
    ) {
    }

    private record PaymentRow(UUID id, UUID orderId, String status) {
    }
}
