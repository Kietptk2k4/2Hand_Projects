package com.twohands.social_service.application.post.viewpostdetail;

import java.math.BigDecimal;
import java.util.List;

public record ViewPostDetailResult(
        String postId,
        AuthorSummary author,
        String caption,
        List<MediaItemData> media,
        List<ProductTagData> productTags,
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
    public record AuthorSummary(
            String userId,
            String displayName,
            String avatarUrl
    ) {
    }

    public record MediaItemData(
            String url,
            String type,
            Integer width,
            Integer height
    ) {
    }

    public record ProductTagData(
            String productId,
            BigDecimal price
    ) {
    }
}
