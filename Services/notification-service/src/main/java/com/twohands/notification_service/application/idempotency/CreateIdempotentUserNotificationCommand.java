package com.twohands.notification_service.application.idempotency;

import com.twohands.notification_service.domain.usernotification.NotificationDeliveryStatus;

import java.util.UUID;

public record CreateIdempotentUserNotificationCommand(
        UUID notificationEventId,
        UUID userId,
        UUID actorId,
        String type,
        String title,
        String content,
        String referenceType,
        String referenceId,
        String metadata,
        NotificationDeliveryStatus deliveryStatus
) {
}
