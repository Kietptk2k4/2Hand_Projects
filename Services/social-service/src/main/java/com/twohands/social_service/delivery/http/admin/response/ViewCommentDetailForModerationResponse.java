package com.twohands.social_service.delivery.http.admin.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public record ViewCommentDetailForModerationResponse(
        String id,
        @JsonProperty("post_id")
        String postId,
        AuthorData author,
        @JsonProperty("parent_comment_id")
        String parentCommentId,
        @JsonProperty("parent_comment")
        ParentCommentData parentComment,
        @JsonProperty("content_text")
        String contentText,
        List<MediaData> media,
        @JsonProperty("media_count")
        int mediaCount,
        String status,
        @JsonProperty("moderation_status")
        String moderationStatus,
        @JsonProperty("moderation_reason")
        String moderationReason,
        @JsonProperty("last_moderation_log_id")
        String lastModerationLogId,
        @JsonProperty("like_count")
        long likeCount,
        PostContextData post,
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

    public record ParentCommentData(
            String id,
            @JsonProperty("content_preview")
            String contentPreview
    ) {
    }

    public record MediaData(
            String url,
            String type
    ) {
    }

    public record PostContextData(
            String id,
            @JsonProperty("caption_preview")
            String captionPreview,
            @JsonProperty("thumbnail_url")
            String thumbnailUrl,
            @JsonProperty("moderation_status")
            String moderationStatus
    ) {
    }
}
