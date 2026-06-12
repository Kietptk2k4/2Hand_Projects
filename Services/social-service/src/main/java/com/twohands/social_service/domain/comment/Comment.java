package com.twohands.social_service.domain.comment;

import java.time.Instant;
import java.util.List;

public record Comment(
        String id,
        String postId,
        String authorId,
        String parentCommentId,
        String contentText,
        List<CommentMediaItem> media,
        CommentStatus status,
        CommentModerationStatus moderationStatus,
        String moderationReason,
        String lastModerationLogId,
        long likeCount,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt
) {
    public CommentModerationStatus moderationStatusOrDefault() {
        return moderationStatus != null ? moderationStatus : CommentModerationStatus.NONE;
    }

    public boolean isPubliclyVisible() {
        return status == CommentStatus.ACTIVE && !moderationStatusOrDefault().isHiddenFromDiscovery();
    }
}
