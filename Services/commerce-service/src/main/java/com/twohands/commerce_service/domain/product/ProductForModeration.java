package com.twohands.commerce_service.domain.product;

import java.util.UUID;

public record ProductForModeration(
        UUID productId,
        UUID sellerId,
        UUID shopId,
        String title,
        ProductStatus status
) {
    public boolean isRemoved() {
        return status == ProductStatus.REMOVED;
    }
}
