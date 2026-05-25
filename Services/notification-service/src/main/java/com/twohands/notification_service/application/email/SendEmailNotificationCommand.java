package com.twohands.notification_service.application.email;

import java.util.UUID;

public record SendEmailNotificationCommand(
        UUID recipientUserId,
        String eventType,
        String payload
) {
}
