package com.twohands.social_service.delivery.http.user.response;

import java.util.List;

public record ViewUserPostsResponse(
        List<UserPostItemResponse> items,
        PageMetaResponse meta
) {
    public record UserPostItemResponse(
            String postId,
            String caption,
            List<MediaItemResponse> media,
            String visibility,
            long likeCount,
            long replyCount,
            List<String> hashtags,
            String createdAt
    ) {
    }

    public record MediaItemResponse(
            String url,
            String type
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
