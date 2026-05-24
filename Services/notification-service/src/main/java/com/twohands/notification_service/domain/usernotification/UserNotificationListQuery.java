package com.twohands.notification_service.domain.usernotification;

import java.util.UUID;

public record UserNotificationListQuery(
        UUID userId,
        int page,
        int size
) {
}
