package com.twohands.notification_service.infrastructure.persistence.notificationsetting;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserNotificationSettingJpaRepository
        extends JpaRepository<UserNotificationSettingEntity, UserNotificationSettingEntityId> {

    Optional<UserNotificationSettingEntity> findByUserIdAndEventType(UUID userId, String eventType);
}
