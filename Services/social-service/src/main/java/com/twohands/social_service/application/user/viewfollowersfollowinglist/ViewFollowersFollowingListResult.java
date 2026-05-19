package com.twohands.social_service.application.user.viewfollowersfollowinglist;

import com.twohands.social_service.domain.follow.RelationListType;
import com.twohands.social_service.domain.post.PageResult;

import java.time.Instant;

public record ViewFollowersFollowingListResult(
        String targetUserId,
        RelationListType type,
        PageResult<RelationUserItem> users
) {
    public record RelationUserItem(
            String userId,
            String displayName,
            String avatarUrl,
            Instant followedAt
    ) {
    }
}
