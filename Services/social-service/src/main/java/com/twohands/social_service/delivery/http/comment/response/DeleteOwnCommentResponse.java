package com.twohands.social_service.delivery.http.comment.response;

public record DeleteOwnCommentResponse(
        String commentId,
        String postId,
        String status,
        String deletedAt,
        String updatedAt
) {
}
