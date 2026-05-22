package com.twohands.commerce_service.domain.review;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record UpdateProductReviewResult(
        UUID reviewId,
        UUID orderItemId,
        UUID sellerId,
        UUID buyerId,
        int rating,
        String comment,
        ReviewStatus status,
        boolean ratingChanged,
        Instant createdAt,
        Instant updatedAt,
        BigDecimal sellerRatingAvg,
        int sellerRatingCount
) {
}
