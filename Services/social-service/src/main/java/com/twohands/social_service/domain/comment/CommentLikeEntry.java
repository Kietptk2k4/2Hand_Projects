package com.twohands.social_service.domain.comment;

import java.time.Instant;
import java.util.UUID;

public record CommentLikeEntry(
        UUID userId,
        Instant likedAt
) {
}