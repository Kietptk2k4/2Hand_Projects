package com.twohands.notification_service.infrastructure.persistence.usernotification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserNotificationJpaRepository extends JpaRepository<UserNotificationEntity, UUID> {

    long countByUserIdAndReadFalseAndDeletedFalse(UUID userId);
}
