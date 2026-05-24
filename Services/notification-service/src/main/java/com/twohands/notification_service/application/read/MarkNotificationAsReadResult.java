package com.twohands.notification_service.application.read;

import java.time.Instant;
import java.util.UUID;

public record MarkNotificationAsReadResult(
        UUID notificationId,
        boolean read,
        Instant readAt,
        boolean alreadyRead
) {
}
