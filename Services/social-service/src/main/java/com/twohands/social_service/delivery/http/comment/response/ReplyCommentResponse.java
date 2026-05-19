package com.twohands.social_service.delivery.http.comment.response;

import java.util.List;

public record ReplyCommentResponse(
        String commentId,
        String postId,
        String parentCommentId,
        String authorId,
        String contentText,
        List<MediaItemResponse> media,
        String status,
        String createdAt,
        String updatedAt
) {
    public record MediaItemResponse(String url, String type) {
    }
}
