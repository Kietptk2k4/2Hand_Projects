package com.twohands.social_service.application.post.saveunsavepost;

public record SaveUnsavePostResult(
        String postId,
        boolean saved
) {
}
