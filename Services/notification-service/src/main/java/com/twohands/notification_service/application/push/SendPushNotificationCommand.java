package com.twohands.notification_service.application.push;

import java.util.UUID;

public record SendPushNotificationCommand(
        UUID recipientUserId,
        String eventType,
        String referenceType,
        String referenceId,
        UUID notificationEventId
) {
}
