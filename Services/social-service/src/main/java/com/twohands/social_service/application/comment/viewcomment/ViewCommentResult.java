package com.twohands.social_service.application.comment.viewcomment;

public record ViewCommentResult(
        String commentId,
        String postId,
        String authorId,
        String status,
        String moderationStatus
) {
}
