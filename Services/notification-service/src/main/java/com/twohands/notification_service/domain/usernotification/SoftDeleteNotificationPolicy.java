package com.twohands.notification_service.domain.usernotification;

public final class SoftDeleteNotificationPolicy {

    private SoftDeleteNotificationPolicy() {
    }

    public static SoftDeleteNotificationOutcome apply(UserNotification notification) {
        if (notification.deleted()) {
            return new SoftDeleteNotificationOutcome(notification, false);
        }

        UserNotification deleted = new UserNotification(
                notification.id(),
                notification.notificationEventId(),
                notification.userId(),
                notification.actorId(),
                notification.type(),
                notification.title(),
                notification.content(),
                notification.referenceType(),
                notification.referenceId(),
                notification.read(),
                true,
                notification.metadata(),
                notification.deliveryStatus(),
                notification.createdAt(),
                notification.readAt()
        );

        return new SoftDeleteNotificationOutcome(deleted, true);
    }
}
