package com.twohands.commerce_service.domain.review;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UpdateProductReviewRepository {

    Optional<UpdateProductReviewSnapshot> findByIdAndBuyerId(UUID reviewId, UUID buyerId);

    UpdateProductReviewResult updateReview(UpdateProductReviewDraft draft, Instant updatedAt);

    SellerRatingSummary recalculateSellerRating(UUID sellerId, Instant occurredAt);

    SellerRatingSummary loadSellerRatingSummary(UUID sellerId);
}
