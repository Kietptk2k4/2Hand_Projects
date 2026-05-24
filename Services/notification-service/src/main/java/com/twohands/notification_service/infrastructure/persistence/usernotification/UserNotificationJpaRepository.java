package com.twohands.notification_service.infrastructure.persistence.usernotification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserNotificationJpaRepository extends JpaRepository<UserNotificationEntity, UUID> {

    long countByUserIdAndReadFalseAndDeletedFalse(UUID userId);

    Optional<UserNotificationEntity> findByIdAndUserIdAndDeletedFalse(UUID id, UUID userId);

    Optional<UserNotificationEntity> findByIdAndUserId(UUID id, UUID userId);

    Page<UserNotificationEntity> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<UserNotificationEntity> findByUserIdAndReadFalseAndDeletedFalseOrderByCreatedAtDesc(
            UUID userId,
            Pageable pageable
    );

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE UserNotificationEntity n
            SET n.read = true, n.readAt = :readAt
            WHERE n.userId = :userId AND n.read = false AND n.deleted = false
            """)
    int markAllUnreadVisibleAsReadByUserId(@Param("userId") UUID userId, @Param("readAt") Instant readAt);

    Optional<UserNotificationEntity> findByNotificationEventIdAndUserIdAndTypeAndReferenceTypeAndReferenceId(
            UUID notificationEventId,
            UUID userId,
            String type,
            String referenceType,
            String referenceId
    );
}
