package com.twohands.commerce_service.domain.discovery;

import com.twohands.commerce_service.domain.product.ProductStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductCardSummary(
        UUID productId,
        String title,
        String thumbnailUrl,
        UUID shopId,
        String shopName,
        UUID categoryId,
        String condition,
        ProductStatus status,
        BigDecimal price,
        BigDecimal salePrice,
        BigDecimal effectivePrice,
        boolean inStock,
        boolean lowStock,
        BigDecimal ratingAvg,
        int ratingCount,
        boolean shopVacation,
        String vacationMessage
) {
}
