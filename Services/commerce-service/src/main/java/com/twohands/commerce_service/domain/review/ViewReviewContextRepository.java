package com.twohands.commerce_service.domain.review;

import java.util.Optional;
import java.util.UUID;

public interface ViewReviewContextRepository {

    Optional<ReviewContextSnapshot> findByOrderItemIdAndBuyerId(UUID orderItemId, UUID buyerId);
}
