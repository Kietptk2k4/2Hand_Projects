package com.twohands.commerce_service.domain.review;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ModerateReviewRepository {

    Optional<ReviewForModeration> findById(UUID reviewId);

    boolean updateStatus(UUID reviewId, ReviewStatus currentStatus, ReviewStatus newStatus, Instant occurredAt);

    SellerRatingSummary recalculateSellerRating(UUID sellerId, Instant occurredAt);
}
