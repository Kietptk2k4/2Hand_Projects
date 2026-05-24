package com.twohands.notification_service.domain.usernotification;

public record MarkNotificationAsReadOutcome(
        UserNotification notification,
        boolean changed
) {
}
