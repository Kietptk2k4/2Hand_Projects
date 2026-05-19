package com.twohands.social_service.application.post.saveunsavepost;

import java.util.UUID;

public record SaveUnsavePostCommand(
        UUID userId,
        String postId
) {
}
