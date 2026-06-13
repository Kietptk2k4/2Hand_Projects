package com.twohands.notification_service.domain.social;

import java.util.List;
import java.util.UUID;

public record PostCreatedNotificationContext(
        UUID actorId,
        UUID postAuthorId,
        String postId,
        String actorDisplayName,
        List<UUID> followerUserIds
) {
}
