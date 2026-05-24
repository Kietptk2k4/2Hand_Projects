package com.twohands.notification_service.application.read;

import java.util.UUID;

public record MarkAllNotificationsAsReadCommand(
        UUID userId
) {
}
