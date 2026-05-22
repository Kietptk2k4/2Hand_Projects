package com.twohands.commerce_service.domain.product;

public record ProductInventoryState(
        int stockQuantity,
        int lowStockThreshold,
        int reservedQuantity
) {
}
