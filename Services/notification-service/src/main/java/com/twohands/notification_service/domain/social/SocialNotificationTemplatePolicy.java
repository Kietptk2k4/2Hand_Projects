package com.twohands.notification_service.domain.social;

import com.twohands.notification_service.domain.inapp.InAppNotificationTemplate;
import com.twohands.notification_service.domain.push.PushNotificationTemplate;

public final class SocialNotificationTemplatePolicy {

    private static final String FALLBACK_ACTOR_LABEL = "Người bạn đang theo dõi";

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
                "Bài viết mới",
                actor + " đã đăng bài viết mới."
        );
    }

    public static InAppNotificationTemplate avatarUpdatedInApp(String actorDisplayName) {
        String actor = resolveActorLabel(actorDisplayName);
        return new InAppNotificationTemplate(
                "Cập nhật ảnh đại diện",
                actor + " đã cập nhật ảnh đại diện."
        );
    }

    public static PushNotificationTemplate postCreatedPush(String actorDisplayName) {
        String actor = resolveActorLabel(actorDisplayName);
        return new PushNotificationTemplate(
                "Bài viết mới",
                actor + " đã đăng bài viết mới."
        );
    }

    public static PushNotificationTemplate avatarUpdatedPush(String actorDisplayName) {
        String actor = resolveActorLabel(actorDisplayName);
        return new PushNotificationTemplate(
                "Cập nhật ảnh đại diện",
                actor + " đã cập nhật ảnh đại diện."
        );
    }
}
