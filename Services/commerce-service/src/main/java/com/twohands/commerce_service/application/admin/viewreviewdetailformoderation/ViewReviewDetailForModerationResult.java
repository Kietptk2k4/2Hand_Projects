package com.twohands.commerce_service.application.admin.viewreviewdetailformoderation;

import com.twohands.commerce_service.domain.review.ReviewStatus;

import java.time.Instant;
import java.util.UUID;

public record ViewReviewDetailForModerationResult(
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
