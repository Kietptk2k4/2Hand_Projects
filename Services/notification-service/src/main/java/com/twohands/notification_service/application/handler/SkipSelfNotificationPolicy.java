package com.twohands.notification_service.application.handler;

import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
public class SkipSelfNotificationPolicy {

    private static final Set<String> SOCIAL_SELF_SKIP_EVENTS = Set.of(
            "POST_LIKED",
            "USER_FOLLOWED",
            "COMMENT_CREATED",
            "COMMENT_REPLIED",
            "COMMENT_LIKED"
    );

    public boolean shouldSkip(NotificationSourceService sourceService, String eventType, UUID actorId, UUID recipientId) {
        if (actorId == null || recipientId == null) {
            return false;
        }
        if (sourceService != NotificationSourceService.SOCIAL) {
            return false;
        }
        return SOCIAL_SELF_SKIP_EVENTS.contains(eventType) && actorId.equals(recipientId);
    }
}
