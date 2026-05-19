package com.twohands.social_service.application.user.viewfollowersfollowinglist;

import com.twohands.social_service.domain.follow.RelationListType;

import java.util.UUID;

public record ViewFollowersFollowingListCommand(
        UUID viewerId,
        UUID targetUserId,
        RelationListType type,
        int page,
        int size
) {
}
