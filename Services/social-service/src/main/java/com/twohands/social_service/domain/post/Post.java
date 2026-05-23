package com.twohands.social_service.domain.post;

import java.time.Instant;
import java.util.List;

public record Post(
        String id,
        String authorId,
        String caption,
        List<MediaItem> media,
        List<ProductTag> productTags,
        PostStatus status,
        PostVisibility visibility,
        long likeCount,
        long replyCount,
        List<String> hashtags,
        boolean allowComments,
        PostModerationStatus moderationStatus,
        String moderationReason,
        String lastModerationLogId,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt
) {
    public PostModerationStatus moderationStatusOrDefault() {
        return moderationStatus != null ? moderationStatus : PostModerationStatus.NONE;
    }
}
