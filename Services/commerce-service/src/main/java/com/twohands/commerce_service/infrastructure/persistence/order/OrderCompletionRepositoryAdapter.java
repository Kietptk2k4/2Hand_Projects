package com.twohands.commerce_service.infrastructure.persistence.order;

import com.twohands.commerce_service.application.order.common.OrderCompletedOutboxService;
import com.twohands.commerce_service.domain.order.CompleteOrderOutcome;
import com.twohands.commerce_service.domain.order.CompleteOrderResult;
import com.twohands.commerce_service.domain.order.OrderCompletionRepository;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
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
import java.util.List;
import java.util.UUID;

@Repository
public class OrderCompletionRepositoryAdapter implements OrderCompletionRepository {

    private static final Logger log = LoggerFactory.getLogger(OrderCompletionRepositoryAdapter.class);

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final OutboxEventRepository outboxEventRepository;
    private final OrderCompletedOutboxService orderCompletedOutboxService;

    public OrderCompletionRepositoryAdapter(
            NamedParameterJdbcTemplate jdbcTemplate,
            OutboxEventRepository outboxEventRepository,
            OrderCompletedOutboxService orderCompletedOutboxService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.outboxEventRepository = outboxEventRepository;
        this.orderCompletedOutboxService = orderCompletedOutboxService;
    }

    @Override
    @Transactional
    public CompleteOrderResult completeIfEligible(
            UUID orderId,
            String reason,
            String changedBy,
            String completedByOutbox,
            Instant now
    ) {
        OrderRow order = lockOrder(orderId);
        if (order == null) {
            return new CompleteOrderResult(CompleteOrderOutcome.NOT_FOUND, orderId, null);
        }

        if ("COMPLETED".equals(order.status)) {
            return new CompleteOrderResult(CompleteOrderOutcome.ALREADY_COMPLETED, orderId, now);
        }

        if (!allItemsCompleted(orderId)) {
            log.debug("Order {} is not completable: not all items are COMPLETED", orderId);
            return new CompleteOrderResult(CompleteOrderOutcome.NOT_ELIGIBLE, orderId, null);
        }

        PaymentRow payment = lockPaymentByOrderId(orderId);
        String effectivePaymentStatus = resolvePaymentStatus(order, payment);
        if (!"PAID".equals(effectivePaymentStatus)) {
            log.debug(
                    "Order {} is not completable: payment status is {}",
                    orderId,
                    effectivePaymentStatus
            );
            return new CompleteOrderResult(CompleteOrderOutcome.NOT_ELIGIBLE, orderId, null);
        }

        String sql = """
                UPDATE orders
                SET status = 'COMPLETED',
                    payment_status = 'PAID',
                    completed_at = :now,
                    updated_at = :now
                WHERE id = :orderId
                  AND status = 'PROCESSING'
                  AND completed_at IS NULL
                """;
        int updated = jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("orderId", orderId)
                .addValue("now", Timestamp.from(now)));

        if (updated == 0) {
            OrderRow reloaded = lockOrder(orderId);
            if (reloaded != null && "COMPLETED".equals(reloaded.status)) {
                return new CompleteOrderResult(CompleteOrderOutcome.ALREADY_COMPLETED, orderId, now);
            }
            return new CompleteOrderResult(CompleteOrderOutcome.NOT_ELIGIBLE, orderId, null);
        }

        insertOrderStatusHistory(orderId, order.status, reason, changedBy, now);
        List<UUID> sellerIds = findDistinctSellerIds(orderId);
        outboxEventRepository.save(
                orderCompletedOutboxService.build(orderId, order.buyerId, sellerIds, reason, completedByOutbox, now)
        );

        return new CompleteOrderResult(CompleteOrderOutcome.COMPLETED, orderId, now);
    }

    private OrderRow lockOrder(UUID orderId) {
        String sql = """
                SELECT id, status, payment_status, buyer_id
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
                SELECT id, order_id, status, payment_method
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

    private String resolvePaymentStatus(OrderRow order, PaymentRow payment) {
        if (payment != null && "PAID".equals(payment.status)) {
            return "PAID";
        }
        return order.paymentStatus;
    }

    private boolean allItemsCompleted(UUID orderId) {
        String sql = """
                SELECT COUNT(*) = 0
                FROM order_items
                WHERE order_id = :orderId
                  AND status NOT IN ('COMPLETED', 'CANCELLED')
                """;
        Boolean allCompleted = jdbcTemplate.queryForObject(
                sql,
                new MapSqlParameterSource("orderId", orderId),
                Boolean.class
        );
        return Boolean.TRUE.equals(allCompleted);
    }

    private List<UUID> findDistinctSellerIds(UUID orderId) {
        String sql = """
                SELECT DISTINCT seller_id
                FROM order_items
                WHERE order_id = :orderId
                  AND seller_id IS NOT NULL
                """;
        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("orderId", orderId),
                (rs, rowNum) -> UUID.fromString(rs.getString("seller_id"))
        );
    }

    private void insertOrderStatusHistory(
            UUID orderId,
            String oldStatus,
            String reason,
            String changedBy,
            Instant now
    ) {
        String sql = """
                INSERT INTO order_status_history(id, order_id, old_status, new_status, changed_by, note, created_at)
                VALUES (:id, :orderId, CAST(:oldStatus AS order_status), 'COMPLETED', :changedBy, :note, :createdAt)
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", UUID.randomUUID())
                .addValue("orderId", orderId)
                .addValue("oldStatus", oldStatus)
                .addValue("changedBy", changedBy)
                .addValue("note", reason)
                .addValue("createdAt", Timestamp.from(now)));
    }

    private OrderRow mapOrderRow(ResultSet rs) throws SQLException {
        return new OrderRow(
                UUID.fromString(rs.getString("id")),
                rs.getString("status"),
                rs.getString("payment_status"),
                UUID.fromString(rs.getString("buyer_id"))
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

    private record OrderRow(UUID id, String status, String paymentStatus, UUID buyerId) {
    }

    private record PaymentRow(UUID id, UUID orderId, String status, String paymentMethod) {
    }
}
