package com.twohands.notification_service.application.read;

import java.util.UUID;

public record DismissAnnouncementNotificationResult(
        UUID notificationId,
        boolean dismissed,
        boolean alreadyDismissed
) {
}
