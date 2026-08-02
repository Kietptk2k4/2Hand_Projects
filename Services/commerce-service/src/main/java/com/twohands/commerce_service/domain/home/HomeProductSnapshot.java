package com.twohands.commerce_service.domain.home;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record HomeProductSnapshot(
        UUID productId,
        UUID sellerId,
        UUID shopId,
        String shopName,
        UUID categoryId,
        UUID brandId,
        String title,
        String thumbnailUrl,
        BigDecimal effectivePrice,
        Instant createdAt,
        BigDecimal ratingAvg,
        int ratingCount,
        long popularityRaw,
        boolean inStock,
        boolean shopVacation
) {
}
