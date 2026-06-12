package com.twohands.social_service.application.integration.handlecommentmoderatedevent;

import com.twohands.social_service.domain.comment.CommentModerationAction;

import java.time.Instant;
import java.util.UUID;

public record HandleCommentModeratedEventCommand(
        UUID eventId,
        String commentId,
        UUID moderationLogId,
        CommentModerationAction action,
        String reason,
        UUID moderatedBy,
        Instant moderatedAt
) {
}
