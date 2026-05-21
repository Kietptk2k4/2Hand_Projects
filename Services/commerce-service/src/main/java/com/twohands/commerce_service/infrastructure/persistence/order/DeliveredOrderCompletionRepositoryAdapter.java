package com.twohands.commerce_service.infrastructure.persistence.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.application.order.common.PaymentPaidOutboxService;
import com.twohands.commerce_service.domain.order.CompleteOrderOutcome;
import com.twohands.commerce_service.domain.order.CompleteOrderResult;
import com.twohands.commerce_service.domain.order.DeliveredOrderCompletionResult;
import com.twohands.commerce_service.domain.order.DeliveredOrderCompletionRepository;
import com.twohands.commerce_service.domain.order.OrderCompletionRepository;
import com.twohands.commerce_service.domain.order.StaleDeliveredOrderItemCandidate;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
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
public class DeliveredOrderCompletionRepositoryAdapter implements DeliveredOrderCompletionRepository {

    private static final Logger log = LoggerFactory.getLogger(DeliveredOrderCompletionRepositoryAdapter.class);
    private static final String AUTO_COMPLETE_REASON = "AUTO_COMPLETE_DELIVERED_ORDER";
    private static final String CHANGED_BY = "SYSTEM:AUTO_COMPLETE_DELIVERED_ORDER";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final OutboxEventRepository outboxEventRepository;
    private final PaymentPaidOutboxService paymentPaidOutboxService;
    private final OrderCompletionRepository orderCompletionRepository;
    private final ObjectMapper objectMapper;

    public DeliveredOrderCompletionRepositoryAdapter(
            NamedParameterJdbcTemplate jdbcTemplate,
            OutboxEventRepository outboxEventRepository,
            PaymentPaidOutboxService paymentPaidOutboxService,
            OrderCompletionRepository orderCompletionRepository,
            ObjectMapper objectMapper
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.outboxEventRepository = outboxEventRepository;
        this.paymentPaidOutboxService = paymentPaidOutboxService;
        this.orderCompletionRepository = orderCompletionRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<StaleDeliveredOrderItemCandidate> findStaleDeliveredItems(int batchSize, Instant deliveredBefore) {
        String sql = """
                SELECT oi.id AS order_item_id, oi.order_id
                FROM order_items oi
                LEFT JOIN shipments s ON s.id = oi.shipment_id
                WHERE oi.status = 'DELIVERED'
                  AND COALESCE(s.delivered_at, oi.updated_at) < :deliveredBefore
                ORDER BY COALESCE(s.delivered_at, oi.updated_at) ASC
                LIMIT :batchSize
                """;
        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("deliveredBefore", Timestamp.from(deliveredBefore))
                        .addValue("batchSize", batchSize),
                (rs, rowNum) -> new StaleDeliveredOrderItemCandidate(
                        UUID.fromString(rs.getString("order_item_id")),
                        UUID.fromString(rs.getString("order_id"))
                )
        );
    }

    @Override
    @Transactional
    public DeliveredOrderCompletionResult completeDeliveredItemsForOrder(
            UUID orderId,
            List<UUID> orderItemIds,
            Instant now
    ) {
        OrderRow order = lockOrder(orderId);
        if (order == null) {
            return DeliveredOrderCompletionResult.noOp();
        }

        if ("COMPLETED".equals(order.status)) {
            return DeliveredOrderCompletionResult.noOp();
        }

        int itemsCompleted = 0;
        for (UUID orderItemId : orderItemIds) {
            itemsCompleted += completeOrderItem(orderItemId, orderId, now);
        }

        if (itemsCompleted == 0) {
            return new DeliveredOrderCompletionResult(0, false, false, false);
        }

        PaymentRow payment = lockPaymentByOrderId(orderId);
        boolean paymentMarkedPaid = false;
        if (payment != null && "COD".equals(payment.paymentMethod) && "PENDING".equals(payment.status)) {
            paymentMarkedPaid = markCodPaymentPaid(payment, now);
            if (paymentMarkedPaid) {
                syncOrderPaymentStatusPaid(orderId, now);
            }
        }

        CompleteOrderResult completion = orderCompletionRepository.completeIfEligible(
                orderId,
                AUTO_COMPLETE_REASON,
                CHANGED_BY,
                "SYSTEM",
                now
        );
        boolean orderCompleted = completion.outcome() == CompleteOrderOutcome.COMPLETED;

        return new DeliveredOrderCompletionResult(
                itemsCompleted,
                orderCompleted,
                paymentMarkedPaid,
                completion.outcome() == CompleteOrderOutcome.ALREADY_COMPLETED
        );
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

    private int completeOrderItem(UUID orderItemId, UUID orderId, Instant now) {
        String sql = """
                UPDATE order_items
                SET status = 'COMPLETED',
                    completed_at = :now,
                    updated_at = :now
                WHERE id = :orderItemId
                  AND order_id = :orderId
                  AND status = 'DELIVERED'
                """;
        return jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("orderItemId", orderItemId)
                .addValue("orderId", orderId)
                .addValue("now", Timestamp.from(now)));
    }

    private boolean markCodPaymentPaid(PaymentRow payment, Instant now) {
        String sql = """
                UPDATE payments
                SET status = 'PAID',
                    paid_at = :now,
                    updated_at = :now
                WHERE id = :paymentId AND status = 'PENDING'
                """;
        int updated = jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("paymentId", payment.id)
                .addValue("now", Timestamp.from(now)));
        if (updated == 0) {
            return false;
        }

        insertPaymentStatusHistory(payment.id, payment.status, now);
        outboxEventRepository.save(paymentPaidOutboxService.build(
                payment.id,
                payment.orderId,
                AUTO_COMPLETE_REASON,
                now
        ));
        return true;
    }

    private void syncOrderPaymentStatusPaid(UUID orderId, Instant now) {
        String sql = """
                UPDATE orders
                SET payment_status = 'PAID', updated_at = :now
                WHERE id = :orderId AND payment_status = 'PENDING'
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("orderId", orderId)
                .addValue("now", Timestamp.from(now)));
    }

    private void insertPaymentStatusHistory(UUID paymentId, String oldStatus, Instant now) {
        String sql = """
                INSERT INTO payment_status_history(id, payment_id, old_status, new_status, payload, created_at)
                VALUES (:id, :paymentId, CAST(:oldStatus AS payment_status), 'PAID', CAST(:payload AS jsonb), :createdAt)
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
        payload.put("reason", AUTO_COMPLETE_REASON);
        payload.put("processed_at", now.toString());
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Cannot serialize payment history payload", ex);
        }
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
