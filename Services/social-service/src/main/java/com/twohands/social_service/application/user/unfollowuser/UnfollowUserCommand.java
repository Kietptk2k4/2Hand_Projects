package com.twohands.social_service.application.user.unfollowuser;

import java.util.UUID;

public record UnfollowUserCommand(
        UUID followerId,
        UUID followeeId
) {
}
