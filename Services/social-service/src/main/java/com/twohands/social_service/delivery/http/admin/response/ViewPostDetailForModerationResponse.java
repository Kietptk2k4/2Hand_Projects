package com.twohands.social_service.delivery.http.admin.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public record ViewPostDetailForModerationResponse(
        String id,
        AuthorData author,
        String caption,
        List<MediaData> media,
        @JsonProperty("thumbnail_url")
        String thumbnailUrl,
        @JsonProperty("media_count")
        int mediaCount,
        String status,
        @JsonProperty("moderation_status")
        String moderationStatus,
        @JsonProperty("moderation_reason")
        String moderationReason,
        @JsonProperty("last_moderation_log_id")
        String lastModerationLogId,
        String visibility,
        @JsonProperty("like_count")
        long likeCount,
        @JsonProperty("reply_count")
        long replyCount,
        List<String> hashtags,
        @JsonProperty("allow_comments")
        boolean allowComments,
        @JsonProperty("created_at")
        Instant createdAt,
        @JsonProperty("updated_at")
        Instant updatedAt
) {
    public record AuthorData(
            @JsonProperty("user_id")
            String userId,
            @JsonProperty("display_name")
            String displayName,
            @JsonProperty("avatar_url")
            String avatarUrl
    ) {
    }

    public record MediaData(
            String url,
            String type,
            Integer width,
            Integer height
    ) {
    }
}
