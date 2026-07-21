package com.twohands.social_service.delivery.http.post.response;

import java.math.BigDecimal;
import java.util.List;

public record ViewPostDetailResponse(
        String postId,
        AuthorResponse author,
        String caption,
        List<MediaItemResponse> media,
        List<ProductTagResponse> productTags,
        String visibility,
        String status,
        long likeCount,
        long replyCount,
        List<String> hashtags,
        boolean allowComments,
        boolean likedByMe,
        boolean savedByMe,
        boolean isOwner,
        String createdAt,
        String updatedAt
) {
    public record AuthorResponse(
            String userId,
            String displayName,
            String avatarUrl
    ) {
    }

    public record MediaItemResponse(
            String url,
            String type,
            Integer width,
            Integer height
    ) {
    }

    public record ProductTagResponse(
            String productId,
            BigDecimal price,
            String name,
            String imageUrl,
            String category,
            String categoryId,
            String shopId,
            boolean available
    ) {
    }
}
