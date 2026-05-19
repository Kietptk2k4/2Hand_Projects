package com.twohands.social_service.application.comment.deleteowncomment;

import java.util.List;
import java.util.UUID;

public record DeleteOwnCommentCommand(
        UUID actorId,
        List<String> actorRoles,
        String commentId
) {
}
