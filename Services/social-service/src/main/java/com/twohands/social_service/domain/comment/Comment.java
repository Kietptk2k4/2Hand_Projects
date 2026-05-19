package com.twohands.social_service.domain.comment;

import java.time.Instant;
import java.util.List;

public record Comment(
        String id,
        String postId,
        String authorId,
        String parentCommentId,
        String contentText,
        List<CommentMediaItem> media,
        CommentStatus status,
        long likeCount,
        Instant createdAt,
        Instant updatedAt
) {
}
