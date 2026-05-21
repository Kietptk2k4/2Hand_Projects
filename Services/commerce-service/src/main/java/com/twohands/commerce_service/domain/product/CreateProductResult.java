package com.twohands.commerce_service.domain.product;

import java.time.Instant;
import java.util.UUID;

public record CreateProductResult(
        UUID productId,
        UUID sellerId,
        UUID shopId,
        ProductStatus status,
        String productType,
        UUID categoryId,
        UUID brandId,
        String condition,
        String title,
        String description,
        int weightGram,
        Instant createdAt,
        Instant updatedAt
) {
}
