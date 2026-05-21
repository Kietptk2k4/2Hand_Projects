package com.twohands.notification_service.application.ingest;

import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;

import java.util.UUID;

public record NotificationEventIngestCommand(
        UUID sourceEventId,
        String eventKey,
        String eventType,
        NotificationSourceService sourceService,
        String aggregateType,
        String aggregateId,
        UUID actorId,
        UUID recipientUserId,
        String payload
) {
}
