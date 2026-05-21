package com.twohands.commerce_service.domain.shop;

import java.util.UUID;

public record SellerShop(
        UUID id,
        UUID sellerId,
        ShopStatus status
) {
    public boolean isActive() {
        return status == ShopStatus.ACTIVE;
    }

    public boolean canCreateProduct() {
        return status == ShopStatus.ACTIVE;
    }
}
