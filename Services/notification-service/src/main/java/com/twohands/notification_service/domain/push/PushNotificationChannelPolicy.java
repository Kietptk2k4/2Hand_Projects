package com.twohands.notification_service.domain.push;

import com.twohands.notification_service.domain.delivery.DefaultChannelFlags;
import com.twohands.notification_service.domain.delivery.NotificationDefaultChannelPolicy;

public final class PushNotificationChannelPolicy {

    private PushNotificationChannelPolicy() {
    }

    public static boolean supportsPushChannel(String eventType) {
        return NotificationDefaultChannelPolicy.resolve(eventType)
                .map(DefaultChannelFlags::push)
                .orElse(false);
    }
}
