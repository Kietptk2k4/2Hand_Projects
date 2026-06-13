package com.twohands.commerce_service.domain.review;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProductReviewListItem(
        UUID reviewId,
        UUID buyerId,
        String buyerDisplayName,
        String buyerAvatarUrl,
        int rating,
        String comment,
        Instant createdAt,
        List<ReviewMediaItem> media,
        ProductReviewSellerReply sellerReply
) {
}
