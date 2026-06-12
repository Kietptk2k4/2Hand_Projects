package com.twohands.commerce_service.domain.product;

import com.twohands.commerce_service.domain.shop.ShopStatus;

import java.util.UUID;

public record ProductForRestore(
        UUID productId,
        UUID sellerId,
        UUID shopId,
        String title,
        ProductStatus status,
        long stockQuantity,
        ShopStatus shopStatus
) {
    public boolean isRemoved() {
        return status == ProductStatus.REMOVED;
    }
}
