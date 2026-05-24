package com.twohands.notification_service.application.read;

import java.util.UUID;

public record DeleteNotificationResult(
        UUID notificationId,
        boolean deleted,
        boolean alreadyDeleted
) {
}
