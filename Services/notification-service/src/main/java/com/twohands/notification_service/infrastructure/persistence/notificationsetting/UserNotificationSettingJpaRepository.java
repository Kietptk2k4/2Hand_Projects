package com.twohands.notification_service.infrastructure.persistence.notificationsetting;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserNotificationSettingJpaRepository
        extends JpaRepository<UserNotificationSettingEntity, UserNotificationSettingEntityId> {

    Optional<UserNotificationSettingEntity> findByUserIdAndEventType(UUID userId, String eventType);

    List<UserNotificationSettingEntity> findByUserId(UUID userId);

    @Query("SELECT s.eventType FROM UserNotificationSettingEntity s WHERE s.userId = :userId")
    Set<String> findEventTypesByUserId(@Param("userId") UUID userId);
}
