package com.twohands.notification_service.delivery.http.notification.response;

import java.util.UUID;

public record DismissAnnouncementNotificationResponse(
        UUID notificationId,
        boolean dismissed,
        boolean alreadyDismissed
) {
}
