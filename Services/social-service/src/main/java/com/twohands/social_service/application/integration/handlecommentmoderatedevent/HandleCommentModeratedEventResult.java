package com.twohands.social_service.application.integration.handlecommentmoderatedevent;

import com.twohands.social_service.domain.comment.CommentModerationAction;

import java.util.UUID;

public record HandleCommentModeratedEventResult(
        UUID eventId,
        String commentId,
        CommentModerationAction action,
        boolean duplicate,
        boolean commentMissing
) {
}
