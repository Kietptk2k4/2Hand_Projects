package com.twohands.social_service.delivery.http.reaction.response;

import java.util.List;

public record ViewLikeUsersResponse(
        List<LikeUserItemResponse> items,
        PageMetaResponse meta
) {
    public record LikeUserItemResponse(
            String userId,
            String displayName,
            String avatarUrl,
            String likedAt
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