package com.twohands.auth_service.infrastructure.persistence.outbox;

import com.twohands.auth_service.domain.outbox.OutboxEvent;
import com.twohands.auth_service.domain.outbox.OutboxEventRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OutboxEventRepositoryAdapter implements OutboxEventRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public OutboxEventRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public OutboxEvent save(OutboxEvent event) {
        String sql = """
                INSERT INTO outbox_events(id, event_type, source, payload, status, retry_count, created_at, published_at, last_error)
                VALUES (:id, :eventType, :source, CAST(:payload AS jsonb), :status, :retryCount, :createdAt, :publishedAt, :lastError)
                """;

        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", event.id())
                .addValue("eventType", event.eventType())
                .addValue("source", event.source())
                .addValue("payload", event.payload())
                .addValue("status", event.status().name())
                .addValue("retryCount", event.retryCount())
                .addValue("createdAt", event.createdAt())
                .addValue("publishedAt", event.publishedAt())
                .addValue("lastError", event.lastError()));

        return event;
    }
}
