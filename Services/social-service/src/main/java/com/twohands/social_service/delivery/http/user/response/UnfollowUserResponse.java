package com.twohands.social_service.delivery.http.user.response;

public record UnfollowUserResponse(
        String followeeId,
        boolean wasFollowing
) {
}
