package com.twohands.notification_service.infrastructure.persistence.usernotification;

import com.twohands.notification_service.domain.idempotency.UserNotificationIdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserNotificationJpaRepository extends JpaRepository<UserNotificationEntity, UUID> {

    long countByUserIdAndReadFalseAndDeletedFalse(UUID userId);

    Optional<UserNotificationEntity> findByNotificationEventIdAndUserIdAndTypeAndReferenceTypeAndReferenceId(
            UUID notificationEventId,
            UUID userId,
            String type,
            String referenceType,
            String referenceId
    );
}
