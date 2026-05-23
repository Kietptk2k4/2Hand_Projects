package com.twohands.notification_service.application.worker;

import java.util.UUID;

public record MarkNotificationEventFailedResult(
        UUID notificationEventId,
        int retryCount,
        int maxRetryCount,
        boolean permanentFailure,
        boolean updated
) {
}
