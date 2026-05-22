package com.twohands.commerce_service.domain.product;

import java.time.Instant;
import java.util.UUID;

public record UpdateProductInventoryResult(
        UUID productId,
        UUID sellerId,
        UUID shopId,
        ProductStatus status,
        ProductStatus previousStatus,
        boolean statusChanged,
        int stockQuantity,
        int lowStockThreshold,
        int reservedQuantity,
        int cartItemsSynced,
        Instant updatedAt
) {
}
