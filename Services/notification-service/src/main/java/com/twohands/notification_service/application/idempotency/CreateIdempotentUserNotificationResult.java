package com.twohands.notification_service.application.idempotency;

import java.util.UUID;

public record CreateIdempotentUserNotificationResult(
        UUID userNotificationId,
        boolean duplicate
) {
}
