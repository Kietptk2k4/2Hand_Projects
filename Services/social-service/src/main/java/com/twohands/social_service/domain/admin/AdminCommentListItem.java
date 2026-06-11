package com.twohands.social_service.domain.admin;

import java.time.Instant;

public record AdminCommentListItem(
        String id,
        String postId,
        String authorId,
        String contentPreview,
        String status,
        long likeCount,
        Instant createdAt,
        Instant updatedAt
) {
}
