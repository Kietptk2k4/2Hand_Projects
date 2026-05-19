package com.twohands.social_service.application.comment.deleteowncomment;

public record DeleteOwnCommentResult(
        String commentId,
        String postId,
        String status,
        String deletedAt,
        String updatedAt
) {
}
