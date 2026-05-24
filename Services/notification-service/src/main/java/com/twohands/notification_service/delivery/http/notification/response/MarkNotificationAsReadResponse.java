package com.twohands.notification_service.delivery.http.notification.response;

import java.time.Instant;
import java.util.UUID;

public record MarkNotificationAsReadResponse(
        UUID notificationId,
        boolean read,
        Instant readAt,
        boolean alreadyRead
) {
}
