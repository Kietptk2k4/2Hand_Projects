package com.twohands.commerce_service.application.product.updateproductinventory;

import java.util.UUID;

public record UpdateProductInventoryCommand(
        UUID sellerId,
        UUID productId,
        int stockQuantity,
        Integer lowStockThreshold
) {
}
