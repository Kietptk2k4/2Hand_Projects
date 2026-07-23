package com.twohands.commerce_service.domain.admin;

import com.twohands.commerce_service.domain.product.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AdminProductDetailEntry(
        UUID productId,
        UUID sellerId,
        UUID shopId,
        String shopName,
        String title,
        String description,
        ProductStatus status,
        UUID categoryId,
        String categoryName,
        BigDecimal price,
        BigDecimal effectivePrice,
        Integer stockQuantity,
        Instant createdAt,
        Instant updatedAt,
        Instant removedAt,
        String removeReason,
        long openOrderCount,
        List<AdminProductDetailMediaItem> media,
        List<AdminProductDetailAttributeItem> attributes
) {
}
