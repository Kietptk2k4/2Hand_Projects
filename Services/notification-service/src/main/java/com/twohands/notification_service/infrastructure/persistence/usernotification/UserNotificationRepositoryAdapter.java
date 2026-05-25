package com.twohands.notification_service.infrastructure.persistence.usernotification;

import com.twohands.notification_service.domain.common.PageResult;
import com.twohands.notification_service.domain.idempotency.UserNotificationIdempotencyKey;
import com.twohands.notification_service.domain.usernotification.UserNotification;
import com.twohands.notification_service.domain.usernotification.UserNotificationListQuery;
import com.twohands.notification_service.domain.usernotification.UserNotificationRepository;
import org.springframework.data.domain.Page;
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
public class UserNotificationRepositoryAdapter implements UserNotificationRepository {

    private final UserNotificationJpaRepository jpaRepository;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public UserNotificationRepositoryAdapter(
            UserNotificationJpaRepository jpaRepository,
            NamedParameterJdbcTemplate jdbcTemplate
    ) {
        this.jpaRepository = jpaRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UserNotification save(UserNotification notification) {
        UserNotificationEntity entity = jpaRepository.findById(notification.id())
                .orElseGet(UserNotificationEntity::new);
        if (entity.getId() == null) {
            entity.setId(notification.id());
        }
        entity.setNotificationEventId(notification.notificationEventId());
        entity.setUserId(notification.userId());
        entity.setActorId(notification.actorId());
        entity.setType(notification.type());
        entity.setTitle(notification.title());
        entity.setContent(notification.content());
        entity.setReferenceType(notification.referenceType());
        entity.setReferenceId(notification.referenceId());
        entity.setRead(notification.read());
        entity.setDeleted(notification.deleted());
        entity.setMetadata(notification.metadata());
        entity.setDeliveryStatus(notification.deliveryStatus());
        entity.setCreatedAt(notification.createdAt());
        entity.setReadAt(notification.readAt());
        UserNotificationEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<UserNotification> findById(UUID notificationId) {
        return jpaRepository.findById(notificationId).map(this::toDomain);
    }

    @Override
    @Transactional
    public List<UserNotification> findFailedDeliveryCandidates(int batchSize) {
        String sql = """
                SELECT id, notification_event_id, user_id, actor_id, type, title, content,
                       reference_type, reference_id, is_read, is_deleted, metadata,
                       delivery_status, created_at, read_at
                FROM user_notifications
                WHERE delivery_status = 'FAILED'
                  AND is_deleted = false
                ORDER BY created_at ASC
                LIMIT :batchSize
                FOR UPDATE SKIP LOCKED
                """;

        MapSqlParameterSource params = new MapSqlParameterSource("batchSize", batchSize);
        return jdbcTemplate.query(sql, params, (rs, rowNum) -> mapUserNotification(rs));
    }

    @Override
    public Optional<UserNotification> findByIdempotencyKey(UserNotificationIdempotencyKey idempotencyKey) {
        return jpaRepository.findByNotificationEventIdAndUserIdAndTypeAndReferenceTypeAndReferenceId(
                        idempotencyKey.notificationEventId(),
                        idempotencyKey.userId(),
                        idempotencyKey.type(),
                        idempotencyKey.referenceType(),
                        idempotencyKey.referenceId()
                )
                .map(this::toDomain);
    }

    @Override
    public Optional<UserNotification> findVisibleByIdAndUserId(UUID notificationId, UUID userId) {
        return jpaRepository.findByIdAndUserIdAndDeletedFalse(notificationId, userId)
                .map(this::toDomain);
    }

    @Override
    public Optional<UserNotification> findByIdAndUserId(UUID notificationId, UUID userId) {
        return jpaRepository.findByIdAndUserId(notificationId, userId)
                .map(this::toDomain);
    }

    @Override
    public PageResult<UserNotification> findVisibleByUserId(UserNotificationListQuery query) {
        Page<UserNotificationEntity> page = jpaRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(
                query.userId(),
                PageRequest.of(query.page(), query.size())
        );

        List<UserNotification> items = page.getContent().stream()
                .map(this::toDomain)
                .toList();

        return new PageResult<>(
                items,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }

    @Override
    public PageResult<UserNotification> findUnreadVisibleByUserId(UserNotificationListQuery query) {
        Page<UserNotificationEntity> page = jpaRepository.findByUserIdAndReadFalseAndDeletedFalseOrderByCreatedAtDesc(
                query.userId(),
                PageRequest.of(query.page(), query.size())
        );

        List<UserNotification> items = page.getContent().stream()
                .map(this::toDomain)
                .toList();

        return new PageResult<>(
                items,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }

    @Override
    public long countByUserIdAndReadFalseAndDeletedFalse(UUID userId) {
        return jpaRepository.countByUserIdAndReadFalseAndDeletedFalse(userId);
    }

    @Override
    public int markAllUnreadVisibleAsRead(UUID userId, Instant readAt) {
        return jpaRepository.markAllUnreadVisibleAsReadByUserId(userId, readAt);
    }

    private UserNotification toDomain(UserNotificationEntity entity) {
        return new UserNotification(
                entity.getId(),
                entity.getNotificationEventId(),
                entity.getUserId(),
                entity.getActorId(),
                entity.getType(),
                entity.getTitle(),
                entity.getContent(),
                entity.getReferenceType(),
                entity.getReferenceId(),
                entity.isRead(),
                entity.isDeleted(),
                entity.getMetadata(),
                entity.getDeliveryStatus(),
                entity.getCreatedAt(),
                entity.getReadAt()
        );
    }

    private UserNotification mapUserNotification(ResultSet rs) throws SQLException {
        Timestamp readAt = rs.getTimestamp("read_at");
        return new UserNotification(
                UUID.fromString(rs.getString("id")),
                rs.getString("notification_event_id") != null
                        ? UUID.fromString(rs.getString("notification_event_id"))
                        : null,
                UUID.fromString(rs.getString("user_id")),
                rs.getString("actor_id") != null ? UUID.fromString(rs.getString("actor_id")) : null,
                rs.getString("type"),
                rs.getString("title"),
                rs.getString("content"),
                rs.getString("reference_type"),
                rs.getString("reference_id"),
                rs.getBoolean("is_read"),
                rs.getBoolean("is_deleted"),
                rs.getString("metadata"),
                com.twohands.notification_service.domain.usernotification.NotificationDeliveryStatus.valueOf(
                        rs.getString("delivery_status")
                ),
                rs.getTimestamp("created_at").toInstant(),
                readAt != null ? readAt.toInstant() : null
        );
    }
}
