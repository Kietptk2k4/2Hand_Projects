package com.twohands.social_service.application.admin.viewcommentdetailformoderation;

import java.time.Instant;
import java.util.List;

public record ViewCommentDetailForModerationResult(
        String id,
        String postId,
        AuthorSummary author,
        String parentCommentId,
        ParentCommentSummary parentComment,
        String contentText,
        List<MediaItemData> media,
        int mediaCount,
        String status,
        String moderationStatus,
        String moderationReason,
        String lastModerationLogId,
        long likeCount,
        PostContextSummary post,
        Instant createdAt,
        Instant updatedAt
) {
    public record AuthorSummary(
            String userId,
            String displayName,
            String avatarUrl
    ) {
    }

    public record ParentCommentSummary(
            String id,
            String contentPreview
    ) {
    }

    public record MediaItemData(
            String url,
            String type
    ) {
    }

    public record PostContextSummary(
            String id,
            String captionPreview,
            String thumbnailUrl,
            String moderationStatus
    ) {
    }
}
