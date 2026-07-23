package com.twohands.social_service.delivery.http.admin.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public record ViewCommentListForModerationResponse(
        List<ItemData> items,
        PaginationData pagination
) {
    public record ItemData(
            String id,
            @JsonProperty("post_id")
            String postId,
            @JsonProperty("author_id")
            String authorId,
            @JsonProperty("author_display_name")
            String authorDisplayName,
            @JsonProperty("author_avatar_url")
            String authorAvatarUrl,
            @JsonProperty("parent_comment_id")
            String parentCommentId,
            @JsonProperty("content_preview")
            String contentPreview,
            String status,
            @JsonProperty("moderation_status")
            String moderationStatus,
            @JsonProperty("media_count")
            int mediaCount,
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
