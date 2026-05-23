package com.twohands.notification_service.application.worker;

import java.time.Instant;
import java.util.UUID;

public record MarkNotificationEventCompletedResult(
        UUID notificationEventId,
        Instant processedAt,
        boolean updated
) {
}
