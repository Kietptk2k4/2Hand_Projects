package com.twohands.notification_service.domain.usernotification;

import java.time.Instant;

public final class MarkNotificationAsReadPolicy {

    private MarkNotificationAsReadPolicy() {
    }

    public static MarkNotificationAsReadOutcome apply(UserNotification notification, Instant readAt) {
        if (notification.read()) {
            return new MarkNotificationAsReadOutcome(notification, false);
        }

        UserNotification marked = new UserNotification(
                notification.id(),
                notification.notificationEventId(),
                notification.userId(),
                notification.actorId(),
                notification.type(),
                notification.title(),
                notification.content(),
                notification.referenceType(),
                notification.referenceId(),
                true,
                notification.deleted(),
                notification.metadata(),
                notification.deliveryStatus(),
                notification.createdAt(),
                readAt
        );

        return new MarkNotificationAsReadOutcome(marked, true);
    }
}
