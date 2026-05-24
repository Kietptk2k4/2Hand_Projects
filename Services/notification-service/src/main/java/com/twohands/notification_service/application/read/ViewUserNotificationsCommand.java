package com.twohands.notification_service.application.read;

import java.util.UUID;

public record ViewUserNotificationsCommand(
        UUID userId,
        int page,
        int size
) {
}
