package com.twohands.commerce_service.domain.product;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateProductInventorySnapshot(
        UUID productId,
        UUID sellerId,
        UUID shopId,
        ProductStatus status,
        String shopStatus,
        boolean categoryActive,
        BigDecimal activePrice,
        ProductInventoryState inventory
) {
    public boolean hasInventory() {
        return inventory != null;
    }

    public int reservedQuantity() {
        return inventory == null ? 0 : inventory.reservedQuantity();
    }

    public int currentLowStockThreshold() {
        return inventory == null ? 0 : inventory.lowStockThreshold();
    }

    public boolean canRestoreActiveFromOutOfStock() {
        return "ACTIVE".equals(shopStatus) && categoryActive && activePrice != null;
    }
}
