package com.twohands.notification_service.domain.social;

import java.util.List;
import java.util.UUID;

public record UserAvatarUpdatedNotificationContext(
        UUID actorId,
        String avatarUrl,
        String displayName,
        List<UUID> followerUserIds
) {
}
