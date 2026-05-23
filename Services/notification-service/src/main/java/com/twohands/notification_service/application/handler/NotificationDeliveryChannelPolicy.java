package com.twohands.notification_service.application.handler;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class NotificationDeliveryChannelPolicy {

    private static final Set<String> IN_APP_SOCIAL_EVENTS = Set.of(
            "POST_LIKED",
            "USER_FOLLOWED",
            "COMMENT_CREATED",
            "COMMENT_REPLIED",
            "COMMENT_LIKED"
    );

    public boolean allowsInApp(String eventType) {
        return IN_APP_SOCIAL_EVENTS.contains(eventType);
    }
}
