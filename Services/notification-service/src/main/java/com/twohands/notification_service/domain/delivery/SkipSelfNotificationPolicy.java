package com.twohands.notification_service.domain.delivery;

import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;

import java.util.Set;
import java.util.UUID;

public final class SkipSelfNotificationPolicy {

    private static final Set<String> SELF_SKIP_EVENT_TYPES = Set.of(
            "POST_LIKED",
            "COMMENT_LIKED",
            "COMMENT_CREATED",
            "COMMENT_REPLIED",
            "USER_FOLLOWED"
    );

    private SkipSelfNotificationPolicy() {
    }

    public static boolean appliesTo(String eventType) {
        return eventType != null && SELF_SKIP_EVENT_TYPES.contains(eventType);
    }

    public static SkipSelfNotificationOutcome evaluate(
            String eventType,
            NotificationSourceService sourceService,
            UUID actorId,
            UUID recipientId
    ) {
        if (!appliesTo(eventType) || sourceService != NotificationSourceService.SOCIAL) {
            return SkipSelfNotificationOutcome.PROCEED;
        }
        if (actorId == null) {
            return SkipSelfNotificationOutcome.MISSING_ACTOR;
        }
        if (recipientId == null) {
            return SkipSelfNotificationOutcome.PROCEED;
        }
        if (actorId.equals(recipientId)) {
            return SkipSelfNotificationOutcome.SKIP;
        }
        return SkipSelfNotificationOutcome.PROCEED;
    }
}
