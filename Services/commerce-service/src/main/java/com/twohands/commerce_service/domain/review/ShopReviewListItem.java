package com.twohands.commerce_service.domain.review;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ShopReviewListItem(
        UUID reviewId,
        UUID orderItemId,
        String productNameSnapshot,
        int rating,
        String comment,
        ReviewStatus status,
        Instant createdAt,
        List<ReviewMediaItem> media,
        ProductReviewSellerReply sellerReply
) {
}
