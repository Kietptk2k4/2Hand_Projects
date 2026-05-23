package com.twohands.notification_service.application.inapp;

import java.util.UUID;

public record CreateInAppNotificationResult(
        UUID userNotificationId,
        boolean duplicate
) {
}
