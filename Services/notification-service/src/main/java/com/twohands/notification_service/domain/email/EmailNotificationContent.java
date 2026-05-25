package com.twohands.notification_service.domain.email;

public record EmailNotificationContent(
        String to,
        String subject,
        String body
) {
}
