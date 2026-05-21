package com.twohands.commerce_service.infrastructure.persistence.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.application.order.common.InventoryReleasedOutboxService;
import com.twohands.commerce_service.application.order.common.OrderCancelledOutboxService;
import com.twohands.commerce_service.domain.order.BuyerOrderCancelOutcome;
import com.twohands.commerce_service.domain.order.BuyerOrderCancellationResult;
import com.twohands.commerce_service.domain.order.OrderCancellationRepository;
import com.twohands.commerce_service.domain.order.OrderItemQuantity;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
    private static final String CHANGED_BY = "BUYER";
    private static final String CANCELLED_BY_OUTBOX = "BUYER";
    private static final String DEFAULT_REASON = "BUYER_CANCELLED";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final OutboxEventRepository outboxEventRepository;
    private final OrderCancelledOutboxService orderCancelledOutboxService;
    private final InventoryReleasedOutboxService inventoryReleasedOutboxService;
    private final ObjectMapper objectMapper;

    public OrderCancellationRepositoryAdapter(
            NamedParameterJdbcTemplate jdbcTemplate,
            OutboxEventRepository outboxEventRepository,
            OrderCancelledOutboxService orderCancelledOutboxService,
            InventoryReleasedOutboxService inventoryReleasedOutboxService,
            ObjectMapper objectMapper
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.outboxEventRepository = outboxEventRepository;
        this.orderCancelledOutboxService = orderCancelledOutboxService;
        this.inventoryReleasedOutboxService = inventoryReleasedOutboxService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public BuyerOrderCancellationResult cancelByBuyer(UUID orderId, UUID buyerId, String reason, Instant now) {
        OrderRow order = lockOrderForBuyer(orderId, buyerId);
        if (order == null) {
            return new BuyerOrderCancellationResult(BuyerOrderCancelOutcome.NOT_FOUND, orderId, null);
        }

        if ("CANCELLED".equals(order.status)) {
            return new BuyerOrderCancellationResult(BuyerOrderCancelOutcome.ALREADY_CANCELLED, orderId, now);
        }

        if (!isCancellableOrderStatus(order.status) || !PaymentStatus.PENDING.name().equals(order.paymentStatus)) {
            return new BuyerOrderCancellationResult(BuyerOrderCancelOutcome.NOT_CANCELLABLE, orderId, null);
        }

        if (hasShipmentBlockingCancel(orderId)) {
            return new BuyerOrderCancellationResult(BuyerOrderCancelOutcome.NOT_CANCELLABLE, orderId, null);
        }

        PaymentRow payment = lockPaymentByOrderId(orderId);
        if (payment == null || !PaymentStatus.PENDING.name().equals(payment.status)) {
            return new BuyerOrderCancellationResult(BuyerOrderCancelOutcome.NOT_CANCELLABLE, orderId, null);
        }

        String cancelReason = resolveReason(reason);
        List<OrderItemQuantity> pendingItems = findPendingOrderItems(orderId);
        releaseInventoryOrThrow(pendingItems, orderId, now);

        int paymentUpdated = cancelPayment(payment.id, now);
        if (paymentUpdated == 0) {
            return new BuyerOrderCancellationResult(BuyerOrderCancelOutcome.NOT_CANCELLABLE, orderId, null);
        }

        int orderUpdated = cancelOrder(orderId, now);
        if (orderUpdated == 0) {
            return new BuyerOrderCancellationResult(BuyerOrderCancelOutcome.NOT_CANCELLABLE, orderId, null);
        }

        cancelPendingOrderItems(orderId, now);
        insertOrderStatusHistory(orderId, order.status, cancelReason, now);
        insertPaymentStatusHistory(payment.id, payment.status, cancelReason, now);

        outboxEventRepository.save(
                orderCancelledOutboxService.build(orderId, cancelReason, CANCELLED_BY_OUTBOX, now)
        );
        if (!pendingItems.isEmpty()) {
            outboxEventRepository.save(inventoryReleasedOutboxService.build(orderId, pendingItems, now));
        }

        return new BuyerOrderCancellationResult(BuyerOrderCancelOutcome.CANCELLED, orderId, now);
    }

    private OrderRow lockOrderForBuyer(UUID orderId, UUID buyerId) {
        String sql = """
                SELECT id, status, payment_status
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

    private PaymentRow lockPaymentByOrderId(UUID orderId) {
        String sql = """
                SELECT id, order_id, status
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
                SELECT EXISTS (
                    SELECT 1
                    FROM shipments
                    WHERE order_id = :orderId
                      AND status NOT IN ('PENDING', 'CANCELLED')
                )
                """;
        Boolean exists = jdbcTemplate.queryForObject(
                sql,
                new MapSqlParameterSource("orderId", orderId),
                Boolean.class
        );
        return Boolean.TRUE.equals(exists);
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
                  AND status IN ('CREATED', 'AWAITING_PAYMENT')
                  AND payment_status = 'PENDING'
                """;
        return jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("orderId", orderId)
                .addValue("now", Timestamp.from(now)));
    }

    private void cancelPendingOrderItems(UUID orderId, Instant now) {
        String sql = """
                UPDATE order_items
                SET status = 'CANCELLED', updated_at = :now
                WHERE order_id = :orderId AND status = 'PENDING'
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("orderId", orderId)
                .addValue("now", Timestamp.from(now)));
    }

    private List<OrderItemQuantity> findPendingOrderItems(UUID orderId) {
        String sql = """
                SELECT id, product_id, quantity
                FROM order_items
                WHERE order_id = :orderId AND status = 'PENDING'
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

    private void insertOrderStatusHistory(UUID orderId, String oldStatus, String reason, Instant now) {
        String sql = """
                INSERT INTO order_status_history(id, order_id, old_status, new_status, changed_by, note, created_at)
                VALUES (:id, :orderId, CAST(:oldStatus AS order_status), 'CANCELLED', :changedBy, :note, :createdAt)
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", UUID.randomUUID())
                .addValue("orderId", orderId)
                .addValue("oldStatus", oldStatus)
                .addValue("changedBy", CHANGED_BY)
                .addValue("note", reason)
                .addValue("createdAt", Timestamp.from(now)));
    }

    private void insertPaymentStatusHistory(UUID paymentId, String oldStatus, String reason, Instant now) {
        String sql = """
                INSERT INTO payment_status_history(id, payment_id, old_status, new_status, payload, created_at)
                VALUES (:id, :paymentId, CAST(:oldStatus AS payment_status), 'CANCELLED', CAST(:payload AS jsonb), :createdAt)
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", UUID.randomUUID())
                .addValue("paymentId", paymentId)
                .addValue("oldStatus", oldStatus)
                .addValue("payload", buildPaymentHistoryPayload(reason, now))
                .addValue("createdAt", Timestamp.from(now)));
    }

    private String buildPaymentHistoryPayload(String reason, Instant now) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("reason", reason);
        payload.put("cancelled_by", CHANGED_BY);
        payload.put("processed_at", now.toString());
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Cannot serialize payment history payload", ex);
        }
    }

    private String resolveReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return DEFAULT_REASON;
        }
        return reason.trim();
    }

    private boolean isCancellableOrderStatus(String status) {
        return "CREATED".equals(status) || "AWAITING_PAYMENT".equals(status);
    }

    private OrderRow mapOrderRow(ResultSet rs) throws SQLException {
        return new OrderRow(
                UUID.fromString(rs.getString("id")),
                rs.getString("status"),
                rs.getString("payment_status")
        );
    }

    private PaymentRow mapPaymentRow(ResultSet rs) throws SQLException {
        return new PaymentRow(
                UUID.fromString(rs.getString("id")),
                UUID.fromString(rs.getString("order_id")),
                rs.getString("status")
        );
    }

    private record OrderRow(UUID id, String status, String paymentStatus) {
    }

    private record PaymentRow(UUID id, UUID orderId, String status) {
    }
}
