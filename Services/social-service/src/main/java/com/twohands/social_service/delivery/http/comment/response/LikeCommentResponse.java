package com.twohands.social_service.delivery.http.comment.response;

public record LikeCommentResponse(
        String commentId,
        boolean liked,
        long likeCount
) {
}
