package com.twohands.social_service.delivery.http.post.response;

public record LikeUnlikePostResponse(
        String postId,
        boolean liked,
        long likeCount
) {
}
