package com.twohands.commerce_service.infrastructure.persistence.outbox;

import com.twohands.commerce_service.domain.outbox.OutboxEvent;
import com.twohands.commerce_service.domain.outbox.OutboxEventRepository;
import com.twohands.commerce_service.domain.outbox.OutboxStatus;
import com.twohands.commerce_service.infrastructure.persistence.JdbcPgEnumTypes;
import com.twohands.commerce_service.infrastructure.persistence.JdbcSqlDialect;
import com.twohands.commerce_service.infrastructure.persistence.JdbcTimestamps;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class OutboxEventRepositoryAdapter implements OutboxEventRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final JdbcSqlDialect sqlDialect;

    public OutboxEventRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate, JdbcSqlDialect sqlDialect) {
        this.jdbcTemplate = jdbcTemplate;
        this.sqlDialect = sqlDialect;
    }

    @Override
    public OutboxEvent save(OutboxEvent event) {
        String sql = """
                INSERT INTO outbox_events(
                    id, event_type, event_key, aggregate_id, source, payload, status,
                    retry_count, created_at, published_at, last_error
                )
                VALUES (
                    :id, :eventType, :eventKey, :aggregateId, :source,
                    %s, %s, :retryCount, :createdAt, :publishedAt, :lastError
                )
                """.formatted(
                sqlDialect.castJsonb("payload"),
                sqlDialect.castEnum("status", JdbcPgEnumTypes.OUTBOX_STATUS)
        );

        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", event.id())
                .addValue("eventType", event.eventType())
                .addValue("eventKey", event.eventKey())
                .addValue("aggregateId", event.aggregateId())
                .addValue("source", event.source())
                .addValue("payload", event.payload())
                .addValue("status", event.status().name())
                .addValue("retryCount", event.retryCount())
                .addValue("createdAt", JdbcTimestamps.from(event.createdAt()))
                .addValue("publishedAt", JdbcTimestamps.from(event.publishedAt()))
                .addValue("lastError", event.lastError()));

        return event;
    }

    @Override
    public List<OutboxEvent> claimPublishCandidates(int batchSize, int maxRetries) {
        String selectSql = """
                SELECT id, event_type, event_key, aggregate_id, source, payload, status,
                       retry_count, created_at, published_at, last_error
                FROM outbox_events
                WHERE status = %s
                  AND retry_count < :maxRetries
                ORDER BY created_at ASC
                LIMIT :batchSize
                FOR UPDATE SKIP LOCKED
                """.formatted(sqlDialect.castEnum("pendingStatus", JdbcPgEnumTypes.OUTBOX_STATUS));
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("pendingStatus", OutboxStatus.PENDING.name())
                .addValue("maxRetries", maxRetries)
                .addValue("batchSize", batchSize);
        List<OutboxEvent> candidates = jdbcTemplate.query(selectSql, params, (rs, rowNum) -> mapOutboxEvent(rs));
        if (candidates.isEmpty()) {
            return candidates;
        }

        markProcessing(candidates);
        return toProcessing(candidates);
    }

    @Override
    public List<OutboxEvent> claimRetryCandidates(int batchSize, int maxRetries, Instant pendingTimeoutBefore) {
        String selectSql = """
                SELECT id, event_type, event_key, aggregate_id, source, payload, status,
                       retry_count, created_at, published_at, last_error
                FROM outbox_events
                WHERE (
                    status = %s
                    AND retry_count < :maxRetries
                ) OR (
                    status = %s
                    AND created_at <= :pendingTimeoutBefore
                    AND retry_count < :maxRetries
                ) OR (
                    status = %s
                    AND created_at <= :pendingTimeoutBefore
                    AND retry_count < :maxRetries
                )
                ORDER BY created_at ASC
                LIMIT :batchSize
                FOR UPDATE SKIP LOCKED
                """.formatted(
                sqlDialect.castEnum("failedStatus", JdbcPgEnumTypes.OUTBOX_STATUS),
                sqlDialect.castEnum("pendingStatus", JdbcPgEnumTypes.OUTBOX_STATUS),
                sqlDialect.castEnum("processingStatus", JdbcPgEnumTypes.OUTBOX_STATUS)
        );
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("failedStatus", OutboxStatus.FAILED.name())
                .addValue("pendingStatus", OutboxStatus.PENDING.name())
                .addValue("processingStatus", OutboxStatus.PROCESSING.name())
                .addValue("maxRetries", maxRetries)
                .addValue("pendingTimeoutBefore", JdbcTimestamps.from(pendingTimeoutBefore))
                .addValue("batchSize", batchSize);
        List<OutboxEvent> candidates = jdbcTemplate.query(selectSql, params, (rs, rowNum) -> mapOutboxEvent(rs));
        if (candidates.isEmpty()) {
            return candidates;
        }

        markProcessing(candidates);
        return toProcessing(candidates);
    }

    @Override
    public int markPublished(UUID eventId, Instant publishedAt) {
        String sql = """
                UPDATE outbox_events
                SET status = %s,
                    published_at = :publishedAt,
                    last_error = NULL
                WHERE id = :eventId
                """.formatted(sqlDialect.castEnum("publishedStatus", JdbcPgEnumTypes.OUTBOX_STATUS));
        return jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("publishedStatus", OutboxStatus.PUBLISHED.name())
                .addValue("publishedAt", JdbcTimestamps.from(publishedAt))
                .addValue("eventId", eventId));
    }

    @Override
    public int markFailed(UUID eventId, String lastError) {
        String sql = """
                UPDATE outbox_events
                SET status = %s,
                    retry_count = retry_count + 1,
                    last_error = :lastError
                WHERE id = :eventId
                """.formatted(sqlDialect.castEnum("failedStatus", JdbcPgEnumTypes.OUTBOX_STATUS));
        return jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("failedStatus", OutboxStatus.FAILED.name())
                .addValue("lastError", truncateLastError(lastError))
                .addValue("eventId", eventId));
    }

    private void markProcessing(List<OutboxEvent> candidates) {
        String markProcessingSql = """
                UPDATE outbox_events
                SET status = %s
                WHERE id IN (:ids)
                """.formatted(sqlDialect.castEnum("processingStatus", JdbcPgEnumTypes.OUTBOX_STATUS));
        jdbcTemplate.update(markProcessingSql, new MapSqlParameterSource()
                .addValue("processingStatus", OutboxStatus.PROCESSING.name())
                .addValue("ids", candidates.stream().map(OutboxEvent::id).toList()));
    }

    private List<OutboxEvent> toProcessing(List<OutboxEvent> candidates) {
        return candidates.stream()
                .map(event -> new OutboxEvent(
                        event.id(),
                        event.eventType(),
                        event.eventKey(),
                        event.aggregateId(),
                        event.source(),
                        event.payload(),
                        OutboxStatus.PROCESSING,
                        event.retryCount(),
                        event.createdAt(),
                        event.publishedAt(),
                        event.lastError()
                ))
                .toList();
    }

    private OutboxEvent mapOutboxEvent(ResultSet rs) throws SQLException {
        return new OutboxEvent(
                UUID.fromString(rs.getString("id")),
                rs.getString("event_type"),
                rs.getString("event_key"),
                UUID.fromString(rs.getString("aggregate_id")),
                rs.getString("source"),
                rs.getString("payload"),
                OutboxStatus.valueOf(rs.getString("status")),
                rs.getInt("retry_count"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("published_at") == null ? null : rs.getTimestamp("published_at").toInstant(),
                rs.getString("last_error")
        );
    }

    private String truncateLastError(String lastError) {
        if (lastError == null) {
            return "Unknown outbox publish error";
        }
        return lastError.length() > 2000 ? lastError.substring(0, 2000) : lastError;
    }
}
