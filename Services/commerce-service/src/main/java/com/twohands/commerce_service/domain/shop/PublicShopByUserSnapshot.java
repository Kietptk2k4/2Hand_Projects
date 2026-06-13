package com.twohands.commerce_service.domain.shop;

import java.util.UUID;

public record PublicShopByUserSnapshot(
        boolean hasShop,
        UUID shopId,
        String shopName,
        String avatarUrl,
        UUID sellerId
) {
    public static PublicShopByUserSnapshot none() {
        return new PublicShopByUserSnapshot(false, null, null, null, null);
    }
}
