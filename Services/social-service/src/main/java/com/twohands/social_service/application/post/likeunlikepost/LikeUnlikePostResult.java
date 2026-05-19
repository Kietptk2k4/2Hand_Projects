package com.twohands.social_service.application.post.likeunlikepost;

public record LikeUnlikePostResult(
        String postId,
        boolean liked,
        long likeCount
) {
}
