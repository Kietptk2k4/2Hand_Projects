package com.twohands.commerce_service.domain.product;

public record ViewProductDetailInventorySummary(
        int stockQuantity,
        int lowStockThreshold,
        boolean inStock,
        boolean lowStock
) {
}
