package com.twohands.notification_service.delivery.http.notification.response;

import java.time.Instant;
import java.util.UUID;

public record UserNotificationItemResponse(
        UUID id,
        UUID actorId,
        String type,
        String title,
        String content,
        String referenceType,
        String referenceId,
        String metadata,
        boolean read,
        Instant readAt,
        Instant createdAt
) {
}
