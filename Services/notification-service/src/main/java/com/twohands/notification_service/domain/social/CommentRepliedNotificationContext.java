package com.twohands.notification_service.domain.social;

import java.util.UUID;

public record CommentRepliedNotificationContext(
        UUID actorId,
        UUID parentCommentAuthorId,
        String parentCommentId,
        String commentId
) {
}
