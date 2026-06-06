package com.twohands.commerce_service.domain.product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SellerProductListItem(
        UUID productId,
        UUID sellerId,
        UUID shopId,
        ProductStatus status,
        String productType,
        UUID categoryId,
        String categoryName,
        String condition,
        String title,
        String description,
        int weightGram,
        String thumbnailUrl,
        BigDecimal price,
        BigDecimal salePrice,
        BigDecimal effectivePrice,
        Integer stockQuantity,
        Integer lowStockThreshold,
        Instant createdAt,
        Instant updatedAt
) {
}