package com.twohands.notification_service.infrastructure.persistence.notificationevent;

import com.twohands.notification_service.domain.notificationevent.NotificationEvent;
import com.twohands.notification_service.domain.notificationevent.NotificationEventRepository;
import com.twohands.notification_service.domain.notificationevent.NotificationEventRetryBackoffPolicy;
import com.twohands.notification_service.domain.notificationevent.NotificationEventStatus;
import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class NotificationEventRepositoryAdapter implements NotificationEventRepository {

    private final NotificationEventJpaRepository jpaRepository;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public NotificationEventRepositoryAdapter(
            NotificationEventJpaRepository jpaRepository,
            NamedParameterJdbcTemplate jdbcTemplate
    ) {
        this.jpaRepository = jpaRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<NotificationEvent> findById(UUID id) {
        return jpaRepository.findById(id).map(NotificationEventMapper::toDomain);
    }

    @Override
    public NotificationEvent save(NotificationEvent event) {
        NotificationEventEntity saved = jpaRepository.save(NotificationEventMapper.toEntity(event));
        return NotificationEventMapper.toDomain(saved);
    }

    @Override
    public Optional<NotificationEvent> findBySourceServiceAndSourceEventId(
            NotificationSourceService sourceService,
            UUID sourceEventId
    ) {
        return jpaRepository.findBySourceServiceAndSourceEventId(sourceService, sourceEventId)
                .map(NotificationEventMapper::toDomain);
    }

    @Override
    public Optional<NotificationEvent> findBySourceServiceAndEventKey(
            NotificationSourceService sourceService,
            String eventKey
    ) {
        return jpaRepository.findBySourceServiceAndEventKey(sourceService, eventKey)
                .map(NotificationEventMapper::toDomain);
    }

    @Override
    public List<NotificationEvent> findStaleProcessingEvents(Instant lockedBefore, int limit) {
        return jpaRepository.findByStatusAndLockedAtBeforeOrderByLockedAtAsc(
                        NotificationEventStatus.PROCESSING,
                        lockedBefore,
                        PageRequest.of(0, limit)
                )
                .stream()
                .map(NotificationEventMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public List<NotificationEvent> claimPendingEvents(int batchSize, String lockedBy) {
        return claimEventsByStatus(batchSize, lockedBy, NotificationEventStatus.PENDING);
    }

    @Override
    @Transactional
    public List<NotificationEvent> claimRetryableFailedEvents(
            int batchSize,
            String lockedBy,
            Instant now,
            int baseBackoffSeconds,
            int maxBackoffSeconds
    ) {
        String selectSql = """
                SELECT id, source_event_id, event_key, event_type, source_service, aggregate_type, aggregate_id,
                       actor_id, recipient_user_id, payload, status, retry_count, max_retry_count, last_error,
                       locked_at, locked_by, created_at, processed_at
                FROM notification_events
                WHERE status = CAST(:status AS notification_event_status)
                  AND retry_count < max_retry_count
                ORDER BY created_at ASC
                LIMIT :batchSize
                FOR UPDATE SKIP LOCKED
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("status", NotificationEventStatus.FAILED.name())
                .addValue("batchSize", batchSize);

        List<NotificationEvent> candidates = jdbcTemplate.query(selectSql, params, (rs, rowNum) -> mapNotificationEvent(rs));
        List<NotificationEvent> eligible = candidates.stream()
                .filter(event -> NotificationEventRetryBackoffPolicy.isEligibleForRetry(
                        event,
                        now,
                        baseBackoffSeconds,
                        maxBackoffSeconds
                ))
                .toList();
        if (eligible.isEmpty()) {
            return List.of();
        }

        return markClaimedAsProcessing(eligible, lockedBy);
    }

    private List<NotificationEvent> claimEventsByStatus(
            int batchSize,
            String lockedBy,
            NotificationEventStatus status
    ) {
        String selectSql = """
                SELECT id, source_event_id, event_key, event_type, source_service, aggregate_type, aggregate_id,
                       actor_id, recipient_user_id, payload, status, retry_count, max_retry_count, last_error,
                       locked_at, locked_by, created_at, processed_at
                FROM notification_events
                WHERE status = CAST(:status AS notification_event_status)
                ORDER BY created_at ASC
                LIMIT :batchSize
                FOR UPDATE SKIP LOCKED
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("status", status.name())
                .addValue("batchSize", batchSize);

        List<NotificationEvent> candidates = jdbcTemplate.query(selectSql, params, (rs, rowNum) -> mapNotificationEvent(rs));
        if (candidates.isEmpty()) {
            return List.of();
        }

        return markClaimedAsProcessing(candidates, lockedBy);
    }

    private List<NotificationEvent> markClaimedAsProcessing(List<NotificationEvent> candidates, String lockedBy) {
        Instant lockedAt = Instant.now();
        String markProcessingSql = """
                UPDATE notification_events
                SET status = CAST(:processingStatus AS notification_event_status),
                    locked_at = :lockedAt,
                    locked_by = :lockedBy
                WHERE id IN (:ids)
                """;
        jdbcTemplate.update(markProcessingSql, new MapSqlParameterSource()
                .addValue("processingStatus", NotificationEventStatus.PROCESSING.name())
                .addValue("lockedAt", Timestamp.from(lockedAt))
                .addValue("lockedBy", lockedBy)
                .addValue("ids", candidates.stream().map(NotificationEvent::id).toList()));

        return candidates.stream()
                .map(event -> new NotificationEvent(
                        event.id(),
                        event.sourceEventId(),
                        event.eventKey(),
                        event.eventType(),
                        event.sourceService(),
                        event.aggregateType(),
                        event.aggregateId(),
                        event.actorId(),
                        event.recipientUserId(),
                        event.payload(),
                        NotificationEventStatus.PROCESSING,
                        event.retryCount(),
                        event.maxRetryCount(),
                        event.lastError(),
                        lockedAt,
                        lockedBy,
                        event.createdAt(),
                        event.processedAt()
                ))
                .toList();
    }

    private NotificationEvent mapNotificationEvent(ResultSet rs) throws SQLException {
        return new NotificationEvent(
                UUID.fromString(rs.getString("id")),
                readUuid(rs, "source_event_id"),
                rs.getString("event_key"),
                rs.getString("event_type"),
                NotificationSourceService.valueOf(rs.getString("source_service")),
                rs.getString("aggregate_type"),
                rs.getString("aggregate_id"),
                readUuid(rs, "actor_id"),
                readUuid(rs, "recipient_user_id"),
                rs.getString("payload"),
                NotificationEventStatus.valueOf(rs.getString("status")),
                rs.getInt("retry_count"),
                rs.getInt("max_retry_count"),
                rs.getString("last_error"),
                readInstant(rs, "locked_at"),
                rs.getString("locked_by"),
                readInstantRequired(rs, "created_at"),
                readInstant(rs, "processed_at")
        );
    }

    private UUID readUuid(ResultSet rs, String column) throws SQLException {
        String value = rs.getString(column);
        return value == null ? null : UUID.fromString(value);
    }

    private Instant readInstant(ResultSet rs, String column) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(column);
        return timestamp == null ? null : timestamp.toInstant();
    }

    private Instant readInstantRequired(ResultSet rs, String column) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(column);
        return timestamp.toInstant();
    }
}
