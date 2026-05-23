package com.twohands.notification_service.application.handler;

import org.springframework.stereotype.Component;

@Component
public class NotificationContentTemplateService {

    public NotificationContentTemplate render(String eventType) {
        return switch (eventType) {
            case "POST_LIKED" -> new NotificationContentTemplate("New like", "Someone liked your post.");
            case "USER_FOLLOWED" -> new NotificationContentTemplate("New follower", "Someone started following you.");
            case "COMMENT_CREATED" -> new NotificationContentTemplate("New comment", "Someone commented on your post.");
            case "COMMENT_REPLIED" -> new NotificationContentTemplate("New reply", "Someone replied to your comment.");
            case "COMMENT_LIKED" -> new NotificationContentTemplate("Comment liked", "Someone liked your comment.");
            default -> new NotificationContentTemplate("Notification", "You have a new notification.");
        };
    }

    public record NotificationContentTemplate(String title, String content) {
    }
}
