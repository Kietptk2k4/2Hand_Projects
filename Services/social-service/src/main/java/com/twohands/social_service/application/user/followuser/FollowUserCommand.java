package com.twohands.social_service.application.user.followuser;

import java.util.UUID;

public record FollowUserCommand(
        UUID followerId,
        UUID followeeId
) {
}
