package com.twohands.notification_service.application.read;

import java.util.UUID;

public record DismissAnnouncementNotificationCommand(
        UUID userId,
        UUID notificationId
) {
}
