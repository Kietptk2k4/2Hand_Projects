package com.twohands.social_service.application.user.viewsuggestedusers;

import com.twohands.social_service.domain.post.PageResult;

public record ViewSuggestedUsersResult(PageResult<SuggestedUserItem> users) {

    public record SuggestedUserItem(
            String userId,
            String displayName,
            String avatarUrl,
            String followStatus,
            long mutualFollowCount
    ) {
    }
}