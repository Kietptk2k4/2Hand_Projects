package com.twohands.social_service.application.integration.handlepostmoderatedevent;

import com.twohands.social_service.domain.post.PostModerationAction;

import java.time.Instant;
import java.util.UUID;

public record HandlePostModeratedEventCommand(
        UUID eventId,
        String postId,
        UUID moderationLogId,
        PostModerationAction action,
        String reason,
        UUID moderatedBy,
        Instant moderatedAt
) {
}
