package com.twohands.notification_service.infrastructure.persistence.usernotification;

import com.twohands.notification_service.domain.idempotency.UserNotificationIdempotencyKey;
import com.twohands.notification_service.domain.usernotification.UserNotification;
import com.twohands.notification_service.domain.usernotification.UserNotificationRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UserNotificationRepositoryAdapter implements UserNotificationRepository {

    private final UserNotificationJpaRepository jpaRepository;

    public UserNotificationRepositoryAdapter(UserNotificationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public UserNotification save(UserNotification notification) {
        UserNotificationEntity entity = new UserNotificationEntity();
        entity.setId(notification.id());
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
    public long countByUserIdAndReadFalseAndDeletedFalse(UUID userId) {
        return jpaRepository.countByUserIdAndReadFalseAndDeletedFalse(userId);
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
}
