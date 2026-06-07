package com.twohands.social_service.domain.post;

import java.time.Instant;
import java.util.UUID;

public record PostLikeEntry(
        UUID userId,
        Instant likedAt
) {
}