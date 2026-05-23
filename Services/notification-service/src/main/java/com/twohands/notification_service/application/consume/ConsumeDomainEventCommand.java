package com.twohands.notification_service.application.consume;

import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;

import java.time.Instant;
import java.util.UUID;

public record ConsumeDomainEventCommand(
        UUID eventId,
        String eventType,
        NotificationSourceService sourceService,
        String eventKey,
        String aggregateType,
        String aggregateId,
        UUID actorId,
        UUID recipientUserId,
        String payloadJson,
        Instant occurredAt
) {
}
