package com.twohands.notification_service.domain.social;

import java.util.UUID;

public record CommentLikedNotificationContext(
        UUID actorId,
        UUID commentAuthorId,
        String commentId
) {
}
