package com.twohands.social_service.application.comment.common;

public record CommentAuthorSummary(
        String userId,
        String displayName,
        String avatarUrl
) {
}