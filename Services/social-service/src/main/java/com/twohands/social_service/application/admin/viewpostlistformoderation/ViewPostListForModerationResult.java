package com.twohands.social_service.application.admin.viewpostlistformoderation;

import java.time.Instant;
import java.util.List;

public record ViewPostListForModerationResult(
        List<Item> items,
        Pagination pagination
) {
    public record Item(
            String id,
            String authorId,
            String captionPreview,
            String status,
            String moderationStatus,
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
