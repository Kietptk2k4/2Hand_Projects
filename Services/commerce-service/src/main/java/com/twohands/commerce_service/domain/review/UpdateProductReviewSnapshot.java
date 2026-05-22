package com.twohands.commerce_service.domain.review;

import java.util.UUID;

public record UpdateProductReviewSnapshot(
        UUID reviewId,
        UUID orderItemId,
        UUID sellerId,
        UUID buyerId,
        int rating,
        String comment,
        ReviewStatus status
) {
}
