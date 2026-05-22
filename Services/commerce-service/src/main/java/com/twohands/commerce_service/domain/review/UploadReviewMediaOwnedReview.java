package com.twohands.commerce_service.domain.review;

import java.util.UUID;

public record UploadReviewMediaOwnedReview(
        UUID reviewId,
        UUID buyerId,
        ReviewStatus status
) {
}
