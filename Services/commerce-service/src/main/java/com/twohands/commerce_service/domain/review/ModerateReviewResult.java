package com.twohands.commerce_service.domain.review;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ModerateReviewResult(
        UUID reviewId,
        UUID orderItemId,
        UUID sellerId,
        UUID buyerId,
        int rating,
        ReviewStatus status,
        ReviewStatus previousStatus,
        boolean alreadyModerated,
        BigDecimal sellerRatingAvg,
        int sellerRatingCount,
        Instant moderatedAt
) {
}
