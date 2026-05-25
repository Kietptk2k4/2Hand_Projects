package com.twohands.notification_service.application.handler;

import com.twohands.notification_service.domain.delivery.NotificationDefaultChannelPolicy;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class NotificationDeliveryChannelPolicy {

    private static final Set<String> DEDICATED_SOCIAL_NOTIFICATION_EVENTS = Set.of(
            "POST_LIKED",
            "USER_FOLLOWED",
            "COMMENT_CREATED",
            "COMMENT_REPLIED",
            "COMMENT_LIKED"
    );

    private static final Set<String> SOCIAL_IN_APP_EVENTS = Set.of();

    public boolean allowsInApp(String eventType) {
        return NotificationDefaultChannelPolicy.resolve(eventType)
                .map(flags -> flags.inApp())
                .orElse(false);
    }

    public boolean isSocialInAppEvent(String eventType) {
        return SOCIAL_IN_APP_EVENTS.contains(eventType);
    }

    public boolean isDedicatedSocialNotificationEvent(String eventType) {
        return DEDICATED_SOCIAL_NOTIFICATION_EVENTS.contains(eventType);
    }

    public boolean isKnownEventType(String eventType) {
        return NotificationDefaultChannelPolicy.isKnownEventType(eventType);
    }
}
