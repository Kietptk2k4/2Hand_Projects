package com.twohands.social_service.application.integration.handlepostmoderatedevent;

import com.twohands.social_service.domain.post.PostModerationAction;

import java.util.UUID;

public record HandlePostModeratedEventResult(
        UUID eventId,
        String postId,
        PostModerationAction action,
        boolean duplicate,
        boolean postMissing
) {
}
