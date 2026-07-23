package com.twohands.commerce_service.application.admin.viewshopdetailformoderation;

import com.twohands.commerce_service.domain.shop.ShopStatus;

import java.time.Instant;
import java.util.UUID;

public record ViewShopDetailForModerationResult(
        UUID shopId,
        UUID sellerId,
        String shopName,
        String description,
        String logoUrl,
        ShopStatus status,
        Instant createdAt,
        Instant updatedAt,
        long totalProductCount,
        long activeProductCount,
        long openOrderCount
) {
}
