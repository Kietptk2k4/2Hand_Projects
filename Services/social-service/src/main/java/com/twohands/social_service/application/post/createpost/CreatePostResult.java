package com.twohands.social_service.application.post.createpost;

import java.math.BigDecimal;
import java.util.List;

public record CreatePostResult(
        String postId,
        String authorId,
        String caption,
        List<MediaItemData> media,
        List<ProductTagData> productTags,
        String status,
        String visibility,
        boolean allowComments,
        List<String> hashtags,
        String createdAt,
        String updatedAt
) {
    public record MediaItemData(String url, String type, Integer width, Integer height) {
    }

    public record ProductTagData(String productId, BigDecimal price) {
    }
}
