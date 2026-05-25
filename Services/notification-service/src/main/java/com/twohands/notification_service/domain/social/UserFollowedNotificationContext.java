package com.twohands.notification_service.domain.social;

import java.util.UUID;

public record UserFollowedNotificationContext(
        UUID actorId,
        UUID followedUserId
) {
}
