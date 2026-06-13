package com.twohands.commerce_service.domain.review;

import java.time.Instant;
import java.util.UUID;

public record MyProductReviewSnapshot(
        boolean hasReview,
        UUID productId,
        UUID reviewId,
        UUID orderItemId,
        Integer rating,
        String comment,
        ReviewStatus status,
        Instant createdAt,
        Instant updatedAt,
        boolean canEdit
) {
    public static MyProductReviewSnapshot noReview(UUID productId) {
        return new MyProductReviewSnapshot(
                false,
                productId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false
        );
    }
}
