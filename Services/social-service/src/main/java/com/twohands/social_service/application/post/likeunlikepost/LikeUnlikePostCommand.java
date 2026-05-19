package com.twohands.social_service.application.post.likeunlikepost;

import java.util.UUID;

public record LikeUnlikePostCommand(
        UUID userId,
        String postId
) {
}
