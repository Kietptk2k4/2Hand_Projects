package com.twohands.notification_service.domain.notificationevent;

import java.time.Instant;
import java.util.UUID;

public record NotificationEvent(
        UUID id,
        UUID sourceEventId,
        String eventKey,
        String eventType,
        NotificationSourceService sourceService,
        String aggregateType,
        String aggregateId,
        UUID actorId,
        UUID recipientUserId,
        String payload,
        NotificationEventStatus status,
        int retryCount,
        int maxRetryCount,
        String lastError,
        Instant lockedAt,
        String lockedBy,
        Instant createdAt,
        Instant processedAt
) {
}
