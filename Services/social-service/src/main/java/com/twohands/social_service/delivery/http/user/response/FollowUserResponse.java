package com.twohands.social_service.delivery.http.user.response;

import java.time.Instant;

public record FollowUserResponse(
        String followeeId,
        String status,
        Instant createdAt
) {
}
