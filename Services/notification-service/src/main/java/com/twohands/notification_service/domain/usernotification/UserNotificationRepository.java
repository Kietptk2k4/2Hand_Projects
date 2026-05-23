package com.twohands.notification_service.domain.usernotification;

import com.twohands.notification_service.domain.idempotency.UserNotificationIdempotencyKey;

import java.util.Optional;
import java.util.UUID;

public interface UserNotificationRepository {

    UserNotification save(UserNotification notification);

    Optional<UserNotification> findByIdempotencyKey(UserNotificationIdempotencyKey idempotencyKey);

    long countByUserIdAndReadFalseAndDeletedFalse(UUID userId);
}
