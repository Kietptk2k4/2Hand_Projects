package com.twohands.notification_service.infrastructure.persistence.usernotification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserNotificationJpaRepository extends JpaRepository<UserNotificationEntity, UUID> {

    long countByUserIdAndReadFalseAndDeletedFalse(UUID userId);

    Page<UserNotificationEntity> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<UserNotificationEntity> findByUserIdAndReadFalseAndDeletedFalseOrderByCreatedAtDesc(
            UUID userId,
            Pageable pageable
    );

    Optional<UserNotificationEntity> findByNotificationEventIdAndUserIdAndTypeAndReferenceTypeAndReferenceId(
            UUID notificationEventId,
            UUID userId,
            String type,
            String referenceType,
            String referenceId
    );
}
