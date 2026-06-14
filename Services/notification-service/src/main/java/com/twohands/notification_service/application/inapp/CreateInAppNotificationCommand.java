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
        String actorDisplayName,
        String reason
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
        this(notificationEventId, userId, actorId, eventType, referenceType, referenceId, metadata, null, null, null);
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
        this(notificationEventId, userId, actorId, eventType, referenceType, referenceId, metadata, templateVariant, null, null);
    }

    public CreateInAppNotificationCommand(
            UUID notificationEventId,
            UUID userId,
            UUID actorId,
            String eventType,
            String referenceType,
            String referenceId,
            String metadata,
            String templateVariant,
            String reason
    ) {
        this(notificationEventId, userId, actorId, eventType, referenceType, referenceId, metadata, templateVariant, null, reason);
    }
}
