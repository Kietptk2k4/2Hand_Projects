package com.twohands.commerce_service.infrastructure.persistence.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.application.order.common.InventoryReleasedOutboxService;
import com.twohands.commerce_service.application.order.common.OrderCancelledOutboxService;
import com.twohands.commerce_service.application.order.common.PaymentExpiredOutboxService;
import com.twohands.commerce_service.domain.order.ExpiredUnpaidOrderCandidate;
import com.twohands.commerce_service.domain.order.OrderItemQuantity;
import com.twohands.commerce_service.domain.order.UnpaidOrderCancelOutcome;
import com.twohands.commerce_service.domain.order.UnpaidOrderCancellationRepository;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
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
public class UnpaidOrderCancellationRepositoryAdapter implements UnpaidOrderCancellationRepository {

    private static final Logger log = LoggerFactory.getLogger(UnpaidOrderCancellationRepositoryAdapter.class);
    private static final String AUTO_CANCEL_REASON = "AUTO_CANCEL_UNPAID_ORDER";
    private static final String CHANGED_BY = "SYSTEM:AUTO_CANCEL_UNPAID_ORDER";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final OutboxEventRepository outboxEventRepository;
    private final PaymentExpiredOutboxService paymentExpiredOutboxService;
    private final OrderCancelledOutboxService orderCancelledOutboxService;
    private final InventoryReleasedOutboxService inventoryReleasedOutboxService;
    private final ObjectMapper objectMapper;

    public UnpaidOrderCancellationRepositoryAdapter(
            NamedParameterJdbcTemplate jdbcTemplate,
            OutboxEventRepository outboxEventRepository,
            PaymentExpiredOutboxService paymentExpiredOutboxService,
            OrderCancelledOutboxService orderCancelledOutboxService,
            InventoryReleasedOutboxService inventoryReleasedOutboxService,
            ObjectMapper objectMapper
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.outboxEventRepository = outboxEventRepository;
        this.paymentExpiredOutboxService = paymentExpiredOutboxService;
        this.orderCancelledOutboxService = orderCancelledOutboxService;
        this.inventoryReleasedOutboxService = inventoryReleasedOutboxService;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<ExpiredUnpaidOrderCandidate> findExpiredCandidates(
            int batchSize,
            Instant now,
            Instant orderCreatedBefore
    ) {
        String sql = """
                SELECT o.id AS order_id, p.id AS payment_id
                FROM orders o
                INNER JOIN payments p ON p.order_id = o.id
                WHERE o.status IN ('CREATED', 'AWAITING_PAYMENT')
                  AND o.payment_status = 'PENDING'
                  AND p.status = 'PENDING'
                  AND p.payment_method = 'PAYOS'
                  AND (
                    (p.expired_at IS NOT NULL AND p.expired_at < :now)
                    OR (p.checkout_url_expired_at IS NOT NULL AND p.checkout_url_expired_at < :now)
                    OR o.created_at < :orderCreatedBefore
                  )
                ORDER BY o.created_at ASC
                LIMIT :batchSize
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("now", Timestamp.from(now))
                .addValue("orderCreatedBefore", Timestamp.from(orderCreatedBefore))
                .addValue("batchSize", batchSize);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new ExpiredUnpaidOrderCandidate(
                UUID.fromString(rs.getString("order_id")),
                UUID.fromString(rs.getString("payment_id"))
        ));
    }

    @Override
    public boolean hasShipmentBlockingCancel(UUID orderId) {
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

    @Override
    @Transactional
    public UnpaidOrderCancelOutcome cancelExpiredUnpaidOrder(UUID orderId, UUID paymentId, Instant now) {
        OrderRow order = lockOrder(orderId);
        if (order == null) {
            return UnpaidOrderCancelOutcome.SKIPPED_ALREADY_TERMINAL;
        }

        PaymentRow payment = lockPayment(paymentId);
        if (payment == null) {
            return UnpaidOrderCancelOutcome.SKIPPED_ALREADY_TERMINAL;
        }

        if (!payment.orderId.equals(orderId)) {
            log.warn("Payment {} does not belong to order {}", paymentId, orderId);
            return UnpaidOrderCancelOutcome.SKIPPED_ALREADY_TERMINAL;
        }

        if (PaymentMethod.COD.name().equals(payment.paymentMethod)) {
            return UnpaidOrderCancelOutcome.SKIPPED_COD;
        }

        if (!isCancellableOrderStatus(order.status) || !PaymentStatus.PENDING.name().equals(order.paymentStatus)) {
            return UnpaidOrderCancelOutcome.SKIPPED_ALREADY_TERMINAL;
        }

        if (!PaymentStatus.PENDING.name().equals(payment.status)) {
            return UnpaidOrderCancelOutcome.SKIPPED_PAYMENT_NOT_PENDING;
        }

        if (hasShipmentBlockingCancel(orderId)) {
            log.info("Skipping auto-cancel for order {} because shipment has started", orderId);
            return UnpaidOrderCancelOutcome.SKIPPED_SHIPMENT_STARTED;
        }

        List<OrderItemQuantity> pendingItems = findPendingOrderItems(orderId);
        releaseInventoryOrThrow(pendingItems, orderId, now);

        int paymentUpdated = expirePayment(paymentId, now);
        if (paymentUpdated == 0) {
            return UnpaidOrderCancelOutcome.SKIPPED_PAYMENT_NOT_PENDING;
        }

        int orderUpdated = cancelOrder(orderId, order.status, now);
        if (orderUpdated == 0) {
            return UnpaidOrderCancelOutcome.SKIPPED_ALREADY_TERMINAL;
        }

        cancelPendingOrderItems(orderId, now);
        insertOrderStatusHistory(orderId, order.status, now);
        insertPaymentStatusHistory(paymentId, payment.status, now);

        outboxEventRepository.save(paymentExpiredOutboxService.build(paymentId, orderId, now));
        outboxEventRepository.save(orderCancelledOutboxService.build(orderId, AUTO_CANCEL_REASON, now));
        if (!pendingItems.isEmpty()) {
            outboxEventRepository.save(inventoryReleasedOutboxService.build(orderId, pendingItems, now));
        }

        return UnpaidOrderCancelOutcome.CANCELLED;
    }

    private OrderRow lockOrder(UUID orderId) {
        String sql = """
                SELECT id, status, payment_status
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

    private PaymentRow lockPayment(UUID paymentId) {
        String sql = """
                SELECT id, order_id, status, payment_method
                FROM payments
                WHERE id = :paymentId
                FOR UPDATE
                """;
        List<PaymentRow> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("paymentId", paymentId),
                (rs, rowNum) -> mapPaymentRow(rs)
        );
        return rows.isEmpty() ? null : rows.getFirst();
    }

    private int expirePayment(UUID paymentId, Instant now) {
        String sql = """
                UPDATE payments
                SET status = 'EXPIRED', updated_at = :now
                WHERE id = :paymentId AND status = 'PENDING'
                """;
        return jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("paymentId", paymentId)
                .addValue("now", Timestamp.from(now)));
    }

    private int cancelOrder(UUID orderId, String oldStatus, Instant now) {
        String sql = """
                UPDATE orders
                SET status = 'CANCELLED',
                    payment_status = 'EXPIRED',
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

    private void insertOrderStatusHistory(UUID orderId, String oldStatus, Instant now) {
        String sql = """
                INSERT INTO order_status_history(id, order_id, old_status, new_status, changed_by, note, created_at)
                VALUES (:id, :orderId, CAST(:oldStatus AS order_status), 'CANCELLED', :changedBy, :note, :createdAt)
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", UUID.randomUUID())
                .addValue("orderId", orderId)
                .addValue("oldStatus", oldStatus)
                .addValue("changedBy", CHANGED_BY)
                .addValue("note", AUTO_CANCEL_REASON)
                .addValue("createdAt", Timestamp.from(now)));
    }

    private void insertPaymentStatusHistory(UUID paymentId, String oldStatus, Instant now) {
        String sql = """
                INSERT INTO payment_status_history(id, payment_id, old_status, new_status, payload, created_at)
                VALUES (:id, :paymentId, CAST(:oldStatus AS payment_status), 'EXPIRED', CAST(:payload AS jsonb), :createdAt)
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", UUID.randomUUID())
                .addValue("paymentId", paymentId)
                .addValue("oldStatus", oldStatus)
                .addValue("payload", buildPaymentHistoryPayload(now))
                .addValue("createdAt", Timestamp.from(now)));
    }

    private String buildPaymentHistoryPayload(Instant now) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("reason", AUTO_CANCEL_REASON);
        payload.put("processed_at", now.toString());
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Cannot serialize payment history payload", ex);
        }
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
                rs.getString("status"),
                rs.getString("payment_method")
        );
    }

    private record OrderRow(UUID id, String status, String paymentStatus) {
    }

    private record PaymentRow(UUID id, UUID orderId, String status, String paymentMethod) {
    }
}
