package com.twohands.social_service.application.user.followuser;

import com.twohands.social_service.domain.follow.FollowStatus;

import java.time.Instant;
import java.util.UUID;

public record FollowUserResult(
        UUID followeeId,
        FollowStatus status,
        Instant createdAt,
        boolean newlyCreated
) {
}
