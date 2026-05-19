package com.twohands.social_service.infrastructure.persistence.outbox;

import com.twohands.social_service.domain.outbox.OutboxEvent;
import com.twohands.social_service.domain.outbox.OutboxEventRepository;
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
                INSERT INTO outbox_events(id, event_type, aggregate_id, payload, status, retry_count, created_at, published_at)
                VALUES (:id, :eventType, :aggregateId, CAST(:payload AS jsonb), :status, :retryCount, :createdAt, :publishedAt)
                """;

        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", event.id())
                .addValue("eventType", event.eventType())
                .addValue("aggregateId", event.aggregateId())
                .addValue("payload", event.payload())
                .addValue("status", event.status().name())
                .addValue("retryCount", event.retryCount())
                .addValue("createdAt", event.createdAt())
                .addValue("publishedAt", event.publishedAt()));

        return event;
    }
}
