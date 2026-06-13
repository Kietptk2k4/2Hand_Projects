package com.twohands.notification_service.domain.social;

import com.twohands.notification_service.domain.inapp.InAppNotificationTemplate;
import com.twohands.notification_service.domain.push.PushNotificationTemplate;

public final class SocialNotificationTemplatePolicy {

    private static final String FALLBACK_ACTOR_LABEL = "Someone you follow";

    private SocialNotificationTemplatePolicy() {
    }

    public static String resolveActorLabel(String actorDisplayName) {
        if (actorDisplayName != null && !actorDisplayName.isBlank()) {
            return actorDisplayName.trim();
        }
        return FALLBACK_ACTOR_LABEL;
    }

    public static InAppNotificationTemplate postCreatedInApp(String actorDisplayName) {
        String actor = resolveActorLabel(actorDisplayName);
        return new InAppNotificationTemplate(
                "New post",
                actor + " shared a new post."
        );
    }

    public static InAppNotificationTemplate avatarUpdatedInApp(String actorDisplayName) {
        String actor = resolveActorLabel(actorDisplayName);
        return new InAppNotificationTemplate(
                "Avatar updated",
                actor + " updated their profile photo."
        );
    }

    public static PushNotificationTemplate postCreatedPush(String actorDisplayName) {
        String actor = resolveActorLabel(actorDisplayName);
        return new PushNotificationTemplate(
                "New post",
                actor + " shared a new post."
        );
    }

    public static PushNotificationTemplate avatarUpdatedPush(String actorDisplayName) {
        String actor = resolveActorLabel(actorDisplayName);
        return new PushNotificationTemplate(
                "Avatar updated",
                actor + " updated their profile photo."
        );
    }
}
