package com.twohands.social_service.delivery.http.comment.response;

import java.util.List;

public record CommentPostResponse(
        String commentId,
        String postId,
        String parentCommentId,
        String authorId,
        AuthorResponse author,
        String contentText,
        List<MediaItemResponse> media,
        String status,
        String createdAt,
        String updatedAt
) {
    public record AuthorResponse(String userId, String displayName, String avatarUrl) {
    }

    public record MediaItemResponse(String url, String type) {
    }
}
