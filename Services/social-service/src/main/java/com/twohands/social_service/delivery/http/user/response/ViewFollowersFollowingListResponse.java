package com.twohands.social_service.delivery.http.user.response;

import java.util.List;

public record ViewFollowersFollowingListResponse(
        String targetUserId,
        String type,
        List<RelationUserItemResponse> items,
        PageMetaResponse meta
) {
    public record RelationUserItemResponse(
            String userId,
            String displayName,
            String avatarUrl,
            String followedAt
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
