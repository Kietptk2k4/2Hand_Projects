package com.twohands.social_service.domain.comment;

public record CommentListQuery(
        String postId,
        String parentCommentId,
        int page,
        int size,
        CommentSortOrder sort
) {
}
