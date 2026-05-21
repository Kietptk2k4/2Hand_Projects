package com.twohands.commerce_service.domain.review;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface CreateProductReviewRepository {

    Optional<ReviewableOrderItem> findReviewableOrderItem(UUID orderItemId, UUID buyerId);

    boolean existsByOrderItemId(UUID orderItemId);

    CreateProductReviewResult createReview(CreateProductReviewDraft draft, Instant occurredAt);
}
