package com.twohands.social_service.delivery.http.user.response;

import java.util.List;

public record ViewSuggestedUsersResponse(
        List<SuggestedUserItemResponse> items,
        PageMetaResponse meta
) {
    public record SuggestedUserItemResponse(
            String userId,
            String displayName,
            String avatarUrl,
            String followStatus,
            long mutualFollowCount
    ) {
    }

    public record PageMetaResponse(
            long page,
            long size,
            long totalElements,
            long totalPages,
            boolean hasNext
    ) {
    }
}