package com.twohands.notification_service.domain.notificationsetting;

import java.time.Instant;
import java.util.UUID;

public record UserNotificationSetting(
        UUID userId,
        String eventType,
        boolean allowPush,
        boolean allowEmail,
        boolean allowInApp,
        Instant createdAt,
        Instant updatedAt
) {
}
