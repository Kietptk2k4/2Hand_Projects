package com.twohands.notification_service.application.settings;

import java.util.UUID;

public record UpdateNotificationSettingsCommand(
        UUID userId,
        String eventType,
        boolean allowPush,
        boolean allowEmail,
        boolean allowInApp
) {
}
