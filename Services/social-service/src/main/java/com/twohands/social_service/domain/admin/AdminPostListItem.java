package com.twohands.social_service.domain.admin;

import java.time.Instant;

public record AdminPostListItem(
        String id,
        String authorId,
        String authorDisplayName,
        String authorAvatarUrl,
        String captionPreview,
        String thumbnailUrl,
        int mediaCount,
        String status,
        String moderationStatus,
        long likeCount,
        Instant createdAt,
        Instant updatedAt
) {
}
