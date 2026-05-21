package com.twohands.commerce_service.application.cart.addproducttocart;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductSummaryResult(
        UUID productId,
        UUID sellerId,
        UUID shopId,
        String productName,
        String imageUrl,
        BigDecimal price,
        BigDecimal salePrice,
        BigDecimal effectivePrice,
        boolean inStock,
        int availableQuantity
) {
}
