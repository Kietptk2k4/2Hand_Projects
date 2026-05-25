package com.twohands.notification_service.application.announcement;

import com.twohands.notification_service.application.handler.NotificationEventHandlerResult;

public record FanOutSystemAnnouncementResult(
        int notificationsCreated,
        int duplicateNotifications,
        boolean delivered,
        NotificationEventHandlerResult failure
) {
    public boolean hasFailure() {
        return failure != null;
    }
}
