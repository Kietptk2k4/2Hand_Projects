package com.twohands.commerce_service.domain.review;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ViewMyProductReviewRepository {

    boolean isProductBuyerVisible(UUID productId, Instant now);

    Optional<MyProductReviewSnapshot> findBuyerReviewForProduct(UUID buyerId, UUID productId);
}
