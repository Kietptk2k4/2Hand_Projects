package com.twohands.notification_service.application.ingest;

import java.util.UUID;

public record IngestNotificationEventResult(
        UUID notificationEventId,
        boolean duplicate
) {
}
