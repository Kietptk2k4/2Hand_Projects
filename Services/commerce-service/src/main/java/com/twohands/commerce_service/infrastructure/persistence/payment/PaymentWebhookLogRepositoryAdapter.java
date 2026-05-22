package com.twohands.commerce_service.infrastructure.persistence.payment;

import com.twohands.commerce_service.domain.payment.PaymentWebhookLogRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PaymentWebhookLogRepositoryAdapter implements PaymentWebhookLogRepository {

    private static final String PROVIDER = "PAYOS";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PaymentWebhookLogRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public WebhookLogRecord recordPayosWebhook(
            String eventType,
            String payosOrderCode,
            String payloadJson,
            boolean signatureValid
    ) {
        try {
            insertLog(eventType, payosOrderCode, payloadJson, signatureValid, false);
            return new WebhookLogRecord(signatureValid, false);
        } catch (DuplicateKeyException ex) {
            return findByPayosEvent(eventType, payosOrderCode)
                    .orElse(new WebhookLogRecord(signatureValid, true));
        }
    }

    @Override
    public Optional<WebhookLogRecord> findByPayosEvent(String eventType, String payosOrderCode) {
        String sql = """
                SELECT signature_valid, processed
                FROM payment_webhook_logs
                WHERE provider = :provider
                  AND payos_order_code = :payosOrderCode
                  AND event_type = :eventType
                """;
        List<WebhookLogRecord> rows = jdbcTemplate.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("provider", PROVIDER)
                        .addValue("payosOrderCode", payosOrderCode)
                        .addValue("eventType", eventType),
                (rs, rowNum) -> new WebhookLogRecord(
                        rs.getBoolean("signature_valid"),
                        rs.getBoolean("processed")
                )
        );
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.getFirst());
    }

    @Override
    public void markProcessed(String eventType, String payosOrderCode) {
        String sql = """
                UPDATE payment_webhook_logs
                SET processed = TRUE
                WHERE provider = :provider
                  AND payos_order_code = :payosOrderCode
                  AND event_type = :eventType
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("provider", PROVIDER)
                .addValue("payosOrderCode", payosOrderCode)
                .addValue("eventType", eventType));
    }

    private void insertLog(
            String eventType,
            String payosOrderCode,
            String payloadJson,
            boolean signatureValid,
            boolean processed
    ) {
        String sql = """
                INSERT INTO payment_webhook_logs(
                    id, provider, event_type, payos_order_code, payload, signature_valid, processed, created_at
                ) VALUES (
                    gen_random_uuid(), :provider, :eventType, :payosOrderCode,
                    CAST(:payload AS jsonb), :signatureValid, :processed, NOW()
                )
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("provider", PROVIDER)
                .addValue("eventType", eventType)
                .addValue("payosOrderCode", payosOrderCode)
                .addValue("payload", payloadJson)
                .addValue("signatureValid", signatureValid)
                .addValue("processed", processed));
    }
}
