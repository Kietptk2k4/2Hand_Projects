package com.twohands.notification_service.application.delivery;

import java.util.UUID;

public record ApplyNotificationDeliveryRulesCommand(
        UUID userId,
        String eventType
) {
}
