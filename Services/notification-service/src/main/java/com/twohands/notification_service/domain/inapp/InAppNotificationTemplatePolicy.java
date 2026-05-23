package com.twohands.notification_service.domain.inapp;

import java.util.Optional;

public final class InAppNotificationTemplatePolicy {

    private InAppNotificationTemplatePolicy() {
    }

    public static Optional<InAppNotificationTemplate> resolve(String eventType) {
        if (eventType == null || eventType.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(switch (eventType) {
            case "POST_LIKED" -> new InAppNotificationTemplate("New like", "Someone liked your post.");
            case "USER_FOLLOWED" -> new InAppNotificationTemplate("New follower", "Someone started following you.");
            case "COMMENT_CREATED" -> new InAppNotificationTemplate("New comment", "Someone commented on your post.");
            case "COMMENT_REPLIED" -> new InAppNotificationTemplate("New reply", "Someone replied to your comment.");
            case "COMMENT_LIKED" -> new InAppNotificationTemplate("Comment liked", "Someone liked your comment.");
            default -> null;
        });
    }
}
