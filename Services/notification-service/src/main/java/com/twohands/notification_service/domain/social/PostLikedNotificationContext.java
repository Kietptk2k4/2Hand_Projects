package com.twohands.notification_service.domain.social;

import java.util.UUID;

public record PostLikedNotificationContext(
        UUID actorId,
        UUID postAuthorId,
        String postId
) {
}
