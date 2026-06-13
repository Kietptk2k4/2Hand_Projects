package com.twohands.notification_service.application.inapp;

import java.util.UUID;

public record CreateInAppNotificationCommand(
        UUID notificationEventId,
        UUID userId,
        UUID actorId,
        String eventType,
        String referenceType,
        String referenceId,
        String metadata,
        String templateVariant,
        String actorDisplayName
) {
    public CreateInAppNotificationCommand(
            UUID notificationEventId,
            UUID userId,
            UUID actorId,
            String eventType,
            String referenceType,
            String referenceId,
            String metadata
    ) {
        this(notificationEventId, userId, actorId, eventType, referenceType, referenceId, metadata, null, null);
    }

    public CreateInAppNotificationCommand(
            UUID notificationEventId,
            UUID userId,
            UUID actorId,
            String eventType,
            String referenceType,
            String referenceId,
            String metadata,
            String templateVariant
    ) {
        this(notificationEventId, userId, actorId, eventType, referenceType, referenceId, metadata, templateVariant, null);
    }
}
