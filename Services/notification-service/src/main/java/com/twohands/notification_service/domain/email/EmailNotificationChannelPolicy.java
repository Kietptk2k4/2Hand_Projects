package com.twohands.notification_service.domain.email;

import com.twohands.notification_service.domain.delivery.DefaultChannelFlags;
import com.twohands.notification_service.domain.delivery.NotificationDefaultChannelPolicy;

public final class EmailNotificationChannelPolicy {

    private EmailNotificationChannelPolicy() {
    }

    public static boolean supportsEmailChannel(String eventType) {
        return NotificationDefaultChannelPolicy.resolve(eventType)
                .map(DefaultChannelFlags::email)
                .orElse(false);
    }
}
