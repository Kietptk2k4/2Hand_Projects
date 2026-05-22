package com.twohands.commerce_service.application.product.pauseproduct;

import com.twohands.commerce_service.domain.product.ProductStatus;

import java.time.Instant;
import java.util.UUID;

public record PauseProductResult(
        UUID productId,
        UUID shopId,
        ProductStatus status,
        Instant pausedAt,
        int cartItemsInvalidated,
        boolean alreadyPaused
) {
}
