package com.twohands.social_service.delivery.http.search.response;

import java.util.List;

public record SearchPostResponse(
        String keyword,
        List<PostItemResponse> items,
        PageMetaResponse meta
) {
    public record PostItemResponse(
            String postId,
            String authorId,
            String caption,
            List<MediaItemResponse> media,
            String visibility,
            long likeCount,
            long replyCount,
            List<String> hashtags,
            boolean allowComments,
            String createdAt,
            String updatedAt
    ) {
    }

    public record MediaItemResponse(
            String url,
            String type,
            Integer width,
            Integer height
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
