package com.twohands.commerce_service.domain.admin;

import com.twohands.commerce_service.domain.review.ReviewStatus;

import java.time.Instant;
import java.util.UUID;

public record AdminReviewDetailEntry(
        UUID reviewId,
        UUID orderItemId,
        UUID productId,
        String productTitle,
        String productThumbnailUrl,
        UUID buyerId,
        UUID sellerId,
        UUID shopId,
        String shopName,
        int rating,
        String comment,
        ReviewStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
