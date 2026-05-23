package com.twohands.notification_service.application.delivery;

import java.util.UUID;

public record RespectNotificationSettingsResult(
        UUID userId,
        String eventType,
        boolean allowInApp,
        boolean allowPush,
        boolean allowEmail,
        boolean explicitSetting
) {
}
