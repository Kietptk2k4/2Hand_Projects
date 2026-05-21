package com.twohands.commerce_service.infrastructure.persistence.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.domain.payment.CreatePaymentRepository;
import com.twohands.commerce_service.domain.payment.CreatePaymentRequest;
import com.twohands.commerce_service.domain.payment.CreatePaymentResult;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Repository
public class CreatePaymentRepositoryAdapter implements CreatePaymentRepository {

    private static final String CREATE_PAYMENT_NOTE = "CREATE_ORDER";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final int paymentTtlMinutes;

    public CreatePaymentRepositoryAdapter(
            NamedParameterJdbcTemplate jdbcTemplate,
            ObjectMapper objectMapper,
            @Value("${commerce.jobs.auto-cancel-unpaid-order.order-ttl-minutes:30}") int paymentTtlMinutes
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.paymentTtlMinutes = paymentTtlMinutes;
    }

    @Override
    public CreatePaymentResult createPayment(CreatePaymentRequest request) {
        Instant now = request.occurredAt();
        PaymentStatus paymentStatus = PaymentStatus.PENDING;
        insertPayment(request, now);
        insertPaymentStatusHistory(request.paymentId(), null, paymentStatus.name(), now);
        return new CreatePaymentResult(request.paymentId(), paymentStatus);
    }

    private void insertPayment(CreatePaymentRequest request, Instant now) {
        Instant expiredAt = request.paymentMethod() == PaymentMethod.PAYOS
                ? now.plusSeconds(paymentTtlMinutes * 60L)
                : null;

        String sql = """
                INSERT INTO payments(
                    id, order_id, payer_id, amount, currency, payment_method, status,
                    idempotency_key, expired_at, checkout_url_expired_at, created_at, updated_at
                ) VALUES (
                    :paymentId, :orderId, :payerId, :amount, 'VND', CAST(:paymentMethod AS payment_method), 'PENDING',
                    :idempotencyKey, :expiredAt, :checkoutUrlExpiredAt, :now, :now
                )
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("paymentId", request.paymentId())
                .addValue("orderId", request.orderId())
                .addValue("payerId", request.payerId())
                .addValue("amount", request.amount())
                .addValue("paymentMethod", request.paymentMethod().name())
                .addValue("idempotencyKey", request.idempotencyKey())
                .addValue("expiredAt", expiredAt == null ? null : Timestamp.from(expiredAt))
                .addValue("checkoutUrlExpiredAt", expiredAt == null ? null : Timestamp.from(expiredAt))
                .addValue("now", Timestamp.from(now)));
    }

    private void insertPaymentStatusHistory(UUID paymentId, String oldStatus, String newStatus, Instant now) {
        String sql = """
                INSERT INTO payment_status_history(id, payment_id, old_status, new_status, payload, created_at)
                VALUES (
                    :id, :paymentId,
                    CAST(:oldStatus AS payment_status),
                    CAST(:newStatus AS payment_status),
                    CAST(:payload AS jsonb),
                    :createdAt
                )
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", UUID.randomUUID())
                .addValue("paymentId", paymentId)
                .addValue("oldStatus", oldStatus)
                .addValue("newStatus", newStatus)
                .addValue("payload", buildPaymentHistoryPayload(now))
                .addValue("createdAt", Timestamp.from(now)));
    }

    private String buildPaymentHistoryPayload(Instant now) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("reason", CREATE_PAYMENT_NOTE);
        payload.put("processed_at", now.toString());
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new AppException(ErrorCode.INTERNAL_ERROR, "Cannot serialize payment history payload", ex);
        }
    }
}
