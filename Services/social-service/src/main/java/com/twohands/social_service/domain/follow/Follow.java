package com.twohands.social_service.domain.follow;

import java.time.Instant;
import java.util.UUID;

public record Follow(
        UUID followerId,
        UUID followeeId,
        FollowStatus status,
        Instant createdAt
) {
}
