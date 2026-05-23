package com.twohands.notification_service.application.handler;

import com.twohands.notification_service.domain.delivery.NotificationDefaultChannelPolicy;
import org.springframework.stereotype.Component;

@Component
public class NotificationDeliveryChannelPolicy {

    public boolean allowsInApp(String eventType) {
        return NotificationDefaultChannelPolicy.resolve(eventType)
                .map(flags -> flags.inApp())
                .orElse(false);
    }

    public boolean isKnownEventType(String eventType) {
        return NotificationDefaultChannelPolicy.isKnownEventType(eventType);
    }
}
