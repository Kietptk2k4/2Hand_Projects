package com.twohands.notification_service.domain.admin;

import java.util.UUID;

public record PostModeratedNotificationContext(
        UUID authorUserId,
        String postId,
        String moderationAction,
        String moderationReason,
        String referenceType,
        String referenceId,
        String templateVariant
) {
}
