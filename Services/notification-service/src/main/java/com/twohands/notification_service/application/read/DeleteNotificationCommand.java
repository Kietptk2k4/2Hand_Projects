package com.twohands.notification_service.application.read;

import java.util.UUID;

public record DeleteNotificationCommand(
        UUID userId,
        UUID notificationId
) {
}
