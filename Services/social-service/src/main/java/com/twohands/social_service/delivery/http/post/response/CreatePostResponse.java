package com.twohands.social_service.delivery.http.post.response;

import java.math.BigDecimal;
import java.util.List;

public record CreatePostResponse(
        String postId,
        String authorId,
        String caption,
        List<MediaItemResponse> media,
        List<ProductTagResponse> productTags,
        String status,
        String visibility,
        boolean allowComments,
        List<String> hashtags,
        String createdAt,
        String updatedAt
) {
    public record MediaItemResponse(String url, String type, Integer width, Integer height) {
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
