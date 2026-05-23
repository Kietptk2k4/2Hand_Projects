package com.twohands.social_service.delivery.http.comment.response;

import java.util.List;

public record ListPostCommentsResponse(
        List<CommentItemResponse> items,
        PageMetaResponse meta
) {
    public record CommentItemResponse(
            String commentId,
            String postId,
            String parentCommentId,
            AuthorResponse author,
            String contentText,
            List<MediaItemResponse> media,
            long likeCount,
            long replyCount,
            String createdAt,
            String updatedAt
    ) {
    }

    public record AuthorResponse(
            String userId,
            String displayName,
            String avatarUrl
    ) {
    }

    public record MediaItemResponse(
            String url,
            String type
    ) {
    }

    public record PageMetaResponse(
            long page,
            long size,
            long totalElements,
            long totalPages,
            boolean hasNext
    ) {
    }
}
