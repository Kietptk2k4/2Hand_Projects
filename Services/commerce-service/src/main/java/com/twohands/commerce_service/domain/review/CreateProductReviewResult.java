package com.twohands.commerce_service.domain.review;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateProductReviewResult(
        UUID reviewId,
        UUID orderItemId,
        UUID sellerId,
        UUID buyerId,
        int rating,
        String comment,
        ReviewStatus status,
        Instant createdAt,
        BigDecimal sellerRatingAvg,
        int sellerRatingCount
) {
}
