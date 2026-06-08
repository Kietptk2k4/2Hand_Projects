package com.twohands.commerce_service.domain.admin;

import com.twohands.commerce_service.domain.shop.ShopStatus;

import java.time.Instant;
import java.util.UUID;

public record AdminShopListEntry(
        UUID shopId,
        UUID sellerId,
        String shopName,
        String logoUrl,
        ShopStatus status,
        Instant createdAt
) {
}
