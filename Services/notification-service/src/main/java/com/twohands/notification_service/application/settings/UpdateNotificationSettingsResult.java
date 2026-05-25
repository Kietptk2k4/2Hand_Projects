package com.twohands.notification_service.application.settings;

import java.time.Instant;
import java.util.UUID;

public record UpdateNotificationSettingsResult(
        UUID userId,
        String eventType,
        boolean allowPush,
        boolean allowEmail,
        boolean allowInApp,
        Instant updatedAt
) {
}
