package com.twohands.social_service.application.admin.viewpostdetailformoderation;

import java.time.Instant;
import java.util.List;

public record ViewPostDetailForModerationResult(
        String id,
        AuthorSummary author,
        String caption,
        List<MediaItemData> media,
        String thumbnailUrl,
        int mediaCount,
        String status,
        String moderationStatus,
        String moderationReason,
        String lastModerationLogId,
        String visibility,
        long likeCount,
        long replyCount,
        List<String> hashtags,
        boolean allowComments,
        Instant createdAt,
        Instant updatedAt
) {
    public record AuthorSummary(
            String userId,
            String displayName,
            String avatarUrl
    ) {
    }

    public record MediaItemData(
            String url,
            String type,
            Integer width,
            Integer height
    ) {
    }
}
