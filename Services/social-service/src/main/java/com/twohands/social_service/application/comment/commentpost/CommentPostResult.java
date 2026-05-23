package com.twohands.social_service.application.comment.commentpost;

import com.twohands.social_service.domain.comment.CommentMediaItem;

import java.util.List;

public record CommentPostResult(
        String commentId,
        String postId,
        String parentCommentId,
        String authorId,
        String contentText,
        List<CommentMediaItem> media,
        String status,
        String createdAt,
        String updatedAt
) {
}
