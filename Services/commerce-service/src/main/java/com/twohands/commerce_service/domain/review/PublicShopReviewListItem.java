package com.twohands.commerce_service.domain.review;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PublicShopReviewListItem(
        UUID reviewId,
        String productNameSnapshot,
        int rating,
        String comment,
        Instant createdAt,
        List<ReviewMediaItem> media,
        ProductReviewSellerReply sellerReply
) {
}
