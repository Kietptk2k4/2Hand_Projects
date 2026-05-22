package com.twohands.commerce_service.domain.shop;

import java.time.Instant;
import java.util.UUID;

public record ModerateShopResult(
        UUID shopId,
        UUID sellerId,
        String shopName,
        ShopStatus status,
        ShopStatus previousStatus,
        boolean alreadyModerated,
        int cartItemsInvalidated,
        Instant moderatedAt
) {
}
