package com.twohands.notification_service.delivery.http.notification.response;

import java.util.List;

public record ViewUserNotificationsResponse(
        List<UserNotificationItemResponse> items,
        PageMetaResponse meta
) {
}
