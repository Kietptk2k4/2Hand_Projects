package com.twohands.notification_service.domain.usernotification;

import java.util.UUID;

public interface UserNotificationRepository {

    UserNotification save(UserNotification notification);

    long countByUserIdAndReadFalseAndDeletedFalse(UUID userId);
}
