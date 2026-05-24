package com.twohands.notification_service.delivery.http.notification.response;

import java.util.UUID;

public record DeleteNotificationResponse(
        UUID notificationId,
        boolean deleted,
        boolean alreadyDeleted
) {
}
