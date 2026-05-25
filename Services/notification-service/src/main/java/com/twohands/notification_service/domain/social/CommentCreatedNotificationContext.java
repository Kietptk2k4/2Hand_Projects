package com.twohands.notification_service.domain.social;

import java.util.UUID;

public record CommentCreatedNotificationContext(
        UUID actorId,
        UUID postAuthorId,
        String postId,
        String commentId
) {
}
