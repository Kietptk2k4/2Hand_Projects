package com.twohands.admin_service.infrastructure.persistence.outbox;

import com.twohands.admin_service.domain.outbox.OutboxEvent;
import com.twohands.admin_service.domain.outbox.OutboxEventRepository;
import com.twohands.admin_service.domain.outbox.OutboxStatus;
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

	public OutboxEventRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public OutboxEvent save(OutboxEvent event) {
		String sql = """
				INSERT INTO outbox_events(
				    id, event_type, aggregate_id, payload, status,
				    retry_count, created_at, published_at, last_error
				)
				VALUES (
				    :id, :eventType, :aggregateId,
				    CAST(:payload AS jsonb), :status, :retryCount, :createdAt, :publishedAt, :lastError
				)
				""";

		jdbcTemplate.update(sql, new MapSqlParameterSource()
				.addValue("id", event.id())
				.addValue("eventType", event.eventType())
				.addValue("aggregateId", event.aggregateId())
				.addValue("payload", event.payload())
				.addValue("status", event.status().name())
				.addValue("retryCount", event.retryCount())
				.addValue("createdAt", event.createdAt())
				.addValue("publishedAt", event.publishedAt())
				.addValue("lastError", event.lastError()));

		return event;
	}

	@Override
	public List<OutboxEvent> claimPublishCandidates(int batchSize, int maxRetries) {
		String selectSql = """
				SELECT id, event_type, aggregate_id, payload, status,
				       retry_count, created_at, published_at, last_error
				FROM outbox_events
				WHERE status = :pendingStatus
				  AND retry_count < :maxRetries
				ORDER BY created_at ASC
				LIMIT :batchSize
				FOR UPDATE SKIP LOCKED
				""";
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
				SELECT id, event_type, aggregate_id, payload, status,
				       retry_count, created_at, published_at, last_error
				FROM outbox_events
				WHERE (
				    status = :failedStatus
				    AND retry_count < :maxRetries
				) OR (
				    status = :pendingStatus
				    AND created_at <= :pendingTimeoutBefore
				    AND retry_count < :maxRetries
				) OR (
				    status = :processingStatus
				    AND created_at <= :pendingTimeoutBefore
				    AND retry_count < :maxRetries
				)
				ORDER BY created_at ASC
				LIMIT :batchSize
				FOR UPDATE SKIP LOCKED
				""";
		MapSqlParameterSource params = new MapSqlParameterSource()
				.addValue("failedStatus", OutboxStatus.FAILED.name())
				.addValue("pendingStatus", OutboxStatus.PENDING.name())
				.addValue("processingStatus", OutboxStatus.PROCESSING.name())
				.addValue("maxRetries", maxRetries)
				.addValue("pendingTimeoutBefore", pendingTimeoutBefore)
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
				SET status = :publishedStatus,
				    published_at = :publishedAt,
				    last_error = NULL
				WHERE id = :eventId
				""";
		return jdbcTemplate.update(sql, new MapSqlParameterSource()
				.addValue("publishedStatus", OutboxStatus.PUBLISHED.name())
				.addValue("publishedAt", publishedAt)
				.addValue("eventId", eventId));
	}

	@Override
	public int markFailed(UUID eventId, String lastError) {
		String sql = """
				UPDATE outbox_events
				SET status = :failedStatus,
				    retry_count = retry_count + 1,
				    last_error = :lastError
				WHERE id = :eventId
				""";
		return jdbcTemplate.update(sql, new MapSqlParameterSource()
				.addValue("failedStatus", OutboxStatus.FAILED.name())
				.addValue("lastError", truncateLastError(lastError))
				.addValue("eventId", eventId));
	}

	private void markProcessing(List<OutboxEvent> candidates) {
		String markProcessingSql = """
				UPDATE outbox_events
				SET status = :processingStatus
				WHERE id IN (:ids)
				""";
		jdbcTemplate.update(markProcessingSql, new MapSqlParameterSource()
				.addValue("processingStatus", OutboxStatus.PROCESSING.name())
				.addValue("ids", candidates.stream().map(OutboxEvent::id).toList()));
	}

	private List<OutboxEvent> toProcessing(List<OutboxEvent> candidates) {
		return candidates.stream()
				.map(event -> new OutboxEvent(
						event.id(),
						event.eventType(),
						event.aggregateId(),
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
				UUID.fromString(rs.getString("aggregate_id")),
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
