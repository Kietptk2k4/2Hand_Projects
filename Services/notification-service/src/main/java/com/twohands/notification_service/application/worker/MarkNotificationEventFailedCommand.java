package com.twohands.notification_service.application.worker;

import java.util.UUID;

public record MarkNotificationEventFailedCommand(
        UUID notificationEventId,
        String errorMessage,
        NotificationFailurePolicy failurePolicy
) {
}
