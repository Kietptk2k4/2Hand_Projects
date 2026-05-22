package com.twohands.commerce_service.domain.shop;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record UpdateShopProfileResult(
        UUID shopId,
        UUID sellerId,
        String shopName,
        String description,
        String avatarUrl,
        String coverUrl,
        ShopStatus status,
        BigDecimal ratingAvg,
        int ratingCount,
        boolean vacationMode,
        Instant createdAt,
        Instant updatedAt
) {
}
