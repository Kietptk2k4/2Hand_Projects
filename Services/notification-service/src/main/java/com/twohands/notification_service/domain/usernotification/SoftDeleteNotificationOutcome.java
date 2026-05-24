package com.twohands.notification_service.domain.usernotification;

public record SoftDeleteNotificationOutcome(
        UserNotification notification,
        boolean changed
) {
}
