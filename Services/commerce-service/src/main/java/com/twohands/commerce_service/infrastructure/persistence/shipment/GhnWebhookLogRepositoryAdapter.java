package com.twohands.commerce_service.infrastructure.persistence.shipment;

import com.twohands.commerce_service.domain.shipment.GhnWebhookLogRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class GhnWebhookLogRepositoryAdapter implements GhnWebhookLogRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public GhnWebhookLogRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UUID insertLog(String ghnOrderCode, String rawStatus, String payloadJson) {
        UUID logId = UUID.randomUUID();
        String sql = """
                INSERT INTO ghn_webhook_logs(
                    id, ghn_order_code, status, payload, processed, created_at
                ) VALUES (
                    :id, :ghnOrderCode, :status, CAST(:payload AS jsonb), FALSE, NOW()
                )
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", logId)
                .addValue("ghnOrderCode", ghnOrderCode)
                .addValue("status", rawStatus)
                .addValue("payload", payloadJson));
        return logId;
    }

    @Override
    public void markProcessed(UUID logId) {
        String sql = """
                UPDATE ghn_webhook_logs
                SET processed = TRUE
                WHERE id = :logId
                """;
        jdbcTemplate.update(sql, new MapSqlParameterSource("logId", logId));
    }
}
