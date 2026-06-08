package com.twohands.social_service.delivery.http.post.response;

import java.util.List;

public record ViewSavedPostsResponse(
        List<SavedPostItemResponse> items,
        PageMetaResponse meta
) {
    public record SavedPostItemResponse(
            String postId,
            String authorId,
            String caption,
            List<MediaItemResponse> media,
            String visibility,
            long likeCount,
            long replyCount,
            List<String> hashtags,
            boolean allowComments,
            String savedAt,
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
