package com.twohands.notification_service.domain.idempotency;

import java.util.UUID;

public record UserNotificationIdempotencyKey(
        UUID notificationEventId,
        UUID userId,
        String type,
        String referenceType,
        String referenceId
) {

    public static UserNotificationIdempotencyKey of(
            UUID notificationEventId,
            UUID userId,
            String type,
            String referenceType,
            String referenceId
    ) {
        return new UserNotificationIdempotencyKey(
                notificationEventId,
                userId,
                type,
                normalizeReference(referenceType),
                normalizeReference(referenceId)
        );
    }

    public static String normalizeReference(String value) {
        return value == null ? "" : value;
    }
}
