package com.twohands.commerce_service.domain.product;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ViewProductDetailRepository {

    Optional<ViewProductDetailResult> findVisibleByProductId(UUID productId, Instant now);
}
