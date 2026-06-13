package com.twohands.social_service.delivery.http.user.response;

public record ViewSocialProfileResponse(
        String userId,
        String displayName,
        String avatarUrl,
        String coverUrl,
        boolean isPrivate,
        Long followerCount,
        Long followingCount,
        String followStatus,
        boolean canViewFullProfile
) {
}
