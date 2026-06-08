package com.twohands.commerce_service.domain.admin;

import com.twohands.commerce_service.domain.product.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AdminProductListEntry(
        UUID productId,
        UUID sellerId,
        UUID shopId,
        String shopName,
        String title,
        String thumbnailUrl,
        UUID categoryId,
        String categoryName,
        BigDecimal price,
        BigDecimal effectivePrice,
        ProductStatus status,
        Instant createdAt,
        Instant removedAt,
        String removeReason
) {
}
