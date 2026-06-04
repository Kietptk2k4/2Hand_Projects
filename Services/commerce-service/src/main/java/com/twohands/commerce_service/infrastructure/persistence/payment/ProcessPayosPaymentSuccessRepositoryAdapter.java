package com.twohands.commerce_service.infrastructure.persistence.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.application.order.common.PaymentPaidOutboxService;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.payment.ProcessPayosPaymentSuccessRepository;
import com.twohands.commerce_service.domain.payment.ProcessPayosPaymentSuccessResult;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
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
public class ProcessPayosPaymentSuccessRepositoryAdapter implements ProcessPayosPaymentSuccessRepository {

    private static final String PAYOS_SUCCESS_REASON = "PAYOS_WEBHOOK_PAID";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final OutboxEventRepository outboxEventRepository;
    private final PaymentPaidOutboxService paymentPaidOutboxService;
    private final ObjectMapper objectMapper;

    public ProcessPayosPaymentSuccessRepositoryAdapter(
            NamedParameterJdbcTemplate jdbcTemplate,
            OutboxEventRepository outboxEventRepository,
            PaymentPaidOutboxService paymentPaidOutboxService,
            ObjectMapper objectMapper
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.outboxEventRepository = outboxEventRepository;
        this.paymentPaidOutboxService = paymentPaidOutboxService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public ProcessPayosPaymentSuccessResult markPaidByPayosOrderCode(
            String payosOrderCode,
            String reason,
            String changedBy,
            Instant occurredAt
    ) {
        LockedPayosPayment payment = lockPayosPayment(payosOrderCode);
        if (payment == null) {
            return ProcessPayosPaymentSuccessResult.notFound();
        }

        if ("PAID".equals(payment.paymentStatus)) {
            return ProcessPayosPaymentSuccessResult.skippedAlreadyPaid(payment.paymentId, payment.orderId);
        }

        if (!"PENDING".equals(payment.paymentStatus) || !"PAYOS".equals(payment.paymentMethod)) {
            return ProcessPayosPaymentSuccessResult.skippedNotPending(payment.paymentId, payment.orderId);
        }

        String effectiveReason = reason != null && !reason.isBlank() ? reason : PAYOS_SUCCESS_REASON;

        int paymentUpdated = markPaymentPaid(payment.paymentId, occurredAt);
        if (paymentUpdated == 0) {
            LockedPayosPayment reloaded = lockPayosPayment(payosOrderCode);
            if (reloaded != null && "PAID".equals(reloaded.paymentStatus)) {
                return ProcessPayosPaymentSuccessResult.skippedAlreadyPaid(reloaded.paymentId, reloaded.orderId);
            }
            return ProcessPayosPaymentSuccessResult.skippedNotPending(payment.paymentId, payment.orderId);
        }

        insertPaymentStatusHistory(payment.paymentId, payment.paymentStatus, effectiveReason, occurredAt);
        transitionOrderToProcessingPaid(payment.orderId, payment.orderStatus, changedBy, effectiveReason, occurredAt);

        outboxEventRepository.save(paymentPaidOutboxService.build(
                payment.paymentId,
                payment.orderId,
                payment.buyerId,
                effectiveReason,
                occurredAt,
                payment.orderId.toString()
        ));

        return ProcessPayosPaymentSuccessResult.processed(payment.paymentId, payment.orderId, occurredAt);
    }

    private LockedPayosPayment lockPayosPayment(String payosOrderCode) {
        String sql = """
                SELECT p.id AS payment_id,
                       p.order_id,
                       o.buyer_id,
                       p.status::text AS payment_status,
                       p.payment_method::text AS payment_method,
                       o.status::text AS order_status
                FROM payments p
                INNER JOIN orders o ON o.id = p.order_id
                WHERE p.payos_order_code = :payosOrderCode
                FOR UPDATE OF p, o
                """;
        List<LockedPayosPayment> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("payosOrderCode", payosOrderCode),
                this::mapLockedPayosPayment
        );
        return rows.isEmpty() ? null : rows.getFirst();
    }

    private int markPaymentPaid(UUID paymentId, Instant now) {
        String sql = """
                UPDATE payments
                SET status = 'PAID',
                    paid_at = :now,
                    updated_at = :now
                WHERE id = :paymentId
                  AND status = 'PENDING'
                  AND payment_method = 'PAYOS'
                """;
        return jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("paymentId", paymentId)
                .addValue("now", Timestamp.from(now)));
    }

    private void transitionOrderToProcessingPaid(
            UUID orderId,
            String previousOrderStatus,
            String changedBy,
            String reason,
            Instant now
    ) {
        String sql = """
                UPDATE orders
                SET status = 'PROCESSING',
                    payment_status = 'PAID',
                    updated_at = :now
                WHERE id = :orderId
                  AND status = 'AWAITING_PAYMENT'
                  AND payment_status = 'PENDING'
                """;
        int updated = jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("orderId", orderId)
                .addValue("now", Timestamp.from(now)));

        if (updated > 0 && "AWAITING_PAYMENT".equals(previousOrderStatus)) {
            insertOrderStatusHistory(orderId, previousOrderStatus, changedBy, reason, now);
        }
    }

    private void insertOrderStatusHistory(
            UUID orderId,
            String oldStatus,
            String changedBy,
            String note,
            Instant now
    ) {
        String sql = """
                INSERT INTO order_status_history(id, order_id, old_status, new_status, changed_by, note, created_at)
                VALUES (:id, :orderId, CAST(:oldStatus AS order_status), 'PROCESSING', :changedBy, :note, :createdAt)
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", UUID.randomUUID())
                .addValue("orderId", orderId)
                .addValue("oldStatus", oldStatus)
                .addValue("changedBy", changedBy)
                .addValue("note", note)
                .addValue("createdAt", Timestamp.from(now)));
    }

    private void insertPaymentStatusHistory(UUID paymentId, String oldStatus, String reason, Instant now) {
        String sql = """
                INSERT INTO payment_status_history(id, payment_id, old_status, new_status, payload, created_at)
                VALUES (:id, :paymentId, CAST(:oldStatus AS payment_status), 'PAID', CAST(:payload AS jsonb), :createdAt)
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
        payload.put("processed_at", now.toString());
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Cannot serialize payment history payload", ex);
        }
    }

    private LockedPayosPayment mapLockedPayosPayment(ResultSet rs, int rowNum) throws SQLException {
        return new LockedPayosPayment(
                UUID.fromString(rs.getString("payment_id")),
                UUID.fromString(rs.getString("order_id")),
                UUID.fromString(rs.getString("buyer_id")),
                rs.getString("payment_status"),
                rs.getString("payment_method"),
                rs.getString("order_status")
        );
    }

    private record LockedPayosPayment(
            UUID paymentId,
            UUID orderId,
            UUID buyerId,
            String paymentStatus,
            String paymentMethod,
            String orderStatus
    ) {
    }
}
