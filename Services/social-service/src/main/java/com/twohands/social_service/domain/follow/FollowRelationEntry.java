package com.twohands.social_service.domain.follow;

import java.time.Instant;
import java.util.UUID;

public record FollowRelationEntry(
        UUID userId,
        Instant followedAt
) {
}
