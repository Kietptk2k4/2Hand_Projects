package com.twohands.notification_service.domain.push;

import java.util.Map;

public record PushNotificationPayload(
        String title,
        String body,
        Map<String, String> data
) {
}
