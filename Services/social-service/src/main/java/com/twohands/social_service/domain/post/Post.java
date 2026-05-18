package com.twohands.social_service.domain.post;

import java.time.Instant;
import java.util.List;

public record Post(
        String id,
        String authorId,
        String caption,
        List<MediaItem> media,
        PostStatus status,
        PostVisibility visibility,
        long likeCount,
        long replyCount,
        List<String> hashtags,
        boolean allowComments,
        Instant createdAt,
        Instant updatedAt
) {
}
