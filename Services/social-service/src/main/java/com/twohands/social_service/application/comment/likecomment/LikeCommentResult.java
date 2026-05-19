package com.twohands.social_service.application.comment.likecomment;

public record LikeCommentResult(
        String commentId,
        boolean liked,
        long likeCount
) {
}
