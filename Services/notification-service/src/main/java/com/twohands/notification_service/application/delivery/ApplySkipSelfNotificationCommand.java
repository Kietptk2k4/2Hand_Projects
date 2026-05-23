package com.twohands.notification_service.application.delivery;

import com.twohands.notification_service.domain.notificationevent.NotificationSourceService;

import java.util.UUID;

public record ApplySkipSelfNotificationCommand(
        String eventType,
        NotificationSourceService sourceService,
        UUID actorId,
        UUID recipientId
) {
}
