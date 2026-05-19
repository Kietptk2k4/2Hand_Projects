package com.twohands.social_service.application.user.viewsocialprofile;

public record ViewSocialProfileResult(
        String userId,
        String displayName,
        String avatarUrl,
        boolean isPrivate,
        Long followerCount,
        Long followingCount,
        String followStatus,
        boolean canViewFullProfile
) {
}
