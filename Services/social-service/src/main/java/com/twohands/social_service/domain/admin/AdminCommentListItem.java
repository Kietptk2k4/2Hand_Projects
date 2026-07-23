package com.twohands.social_service.domain.admin;

import java.time.Instant;

public record AdminCommentListItem(
        String id,
        String postId,
        String authorId,
        String authorDisplayName,
        String authorAvatarUrl,
        String parentCommentId,
        String contentPreview,
        String status,
        String moderationStatus,
        int mediaCount,
        long likeCount,
        Instant createdAt,
        Instant updatedAt
) {
}
