package com.twohands.commerce_service.domain.shop;

import java.time.Instant;
import java.util.UUID;

public record CreateShopResult(
        UUID shopId,
        UUID sellerId,
        String shopName,
        String description,
        String avatarUrl,
        String coverUrl,
        ShopStatus status,
        boolean vacationMode,
        boolean shippingProfileCreated,
        Instant createdAt,
        Instant updatedAt
) {
}
