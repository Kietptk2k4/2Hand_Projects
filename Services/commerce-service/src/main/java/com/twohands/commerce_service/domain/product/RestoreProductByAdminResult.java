package com.twohands.commerce_service.domain.product;

import java.time.Instant;
import java.util.UUID;

public record RestoreProductByAdminResult(
        UUID productId,
        UUID sellerId,
        UUID shopId,
        String title,
        ProductStatus status,
        ProductStatus previousStatus,
        boolean alreadyRestored,
        Instant restoredAt
) {
}
