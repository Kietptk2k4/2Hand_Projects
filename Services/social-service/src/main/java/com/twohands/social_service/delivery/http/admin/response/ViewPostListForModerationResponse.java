package com.twohands.social_service.delivery.http.admin.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public record ViewPostListForModerationResponse(
        List<ItemData> items,
        PaginationData pagination
) {
    public record ItemData(
            String id,
            @JsonProperty("author_id")
            String authorId,
            @JsonProperty("caption_preview")
            String captionPreview,
            String status,
            @JsonProperty("moderation_status")
            String moderationStatus,
            @JsonProperty("like_count")
            long likeCount,
            @JsonProperty("created_at")
            Instant createdAt,
            @JsonProperty("updated_at")
            Instant updatedAt
    ) {
    }

    public record PaginationData(
            int page,
            int size,
            @JsonProperty("total_items")
            long totalItems,
            @JsonProperty("total_pages")
            int totalPages,
            @JsonProperty("has_next")
            boolean hasNext
    ) {
    }
}
