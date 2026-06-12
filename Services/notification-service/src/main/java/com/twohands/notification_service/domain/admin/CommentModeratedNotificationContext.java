package com.twohands.notification_service.domain.admin;

import java.util.UUID;

public record CommentModeratedNotificationContext(
        UUID authorUserId,
        String commentId,
        String postId,
        String action,
        String moderationReason,
        String referenceType,
        String referenceId,
        String templateVariant
) {
}
