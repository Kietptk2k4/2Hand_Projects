package com.twohands.notification_service.application.read;

import java.util.UUID;

public record ViewUnreadNotificationsCommand(
        UUID userId,
        int page,
        int size
) {
}
