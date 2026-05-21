package com.twohands.notification_service.domain.usernotification;

import java.time.Instant;
import java.util.UUID;

public record UserNotification(
        UUID id,
        UUID notificationEventId,
        UUID userId,
        UUID actorId,
        String type,
        String title,
        String content,
        String referenceType,
        String referenceId,
        boolean read,
        boolean deleted,
        String metadata,
        NotificationDeliveryStatus deliveryStatus,
        Instant createdAt,
        Instant readAt
) {
}
