package com.twohands.social_service.application.admin.viewcommentlistformoderation;

import java.time.Instant;
import java.util.List;

public record ViewCommentListForModerationResult(
        List<Item> items,
        Pagination pagination
) {
    public record Item(
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

    public record Pagination(
            int page,
            int size,
            long totalItems,
            int totalPages,
            boolean hasNext
    ) {
    }
}
