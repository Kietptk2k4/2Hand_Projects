package com.twohands.notification_service.delivery.http.internal.response;

import java.util.UUID;

public record IngestNotificationEventResponse(
        UUID notificationEventId,
        boolean duplicate
) {
}
