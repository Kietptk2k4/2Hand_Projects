package com.twohands.notification_service.application.delivery;

import java.util.UUID;

public record RespectNotificationSettingsCommand(
        UUID userId,
        String eventType
) {
}
