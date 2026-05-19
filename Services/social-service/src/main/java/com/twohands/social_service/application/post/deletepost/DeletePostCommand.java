package com.twohands.social_service.application.post.deletepost;

import java.util.List;
import java.util.UUID;

public record DeletePostCommand(
        UUID actorId,
        List<String> actorRoles,
        String postId
) {
}
