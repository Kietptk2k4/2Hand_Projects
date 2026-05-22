package com.twohands.commerce_service.domain.cart;

import java.math.BigDecimal;
import java.util.UUID;

public record ViewCartItem(
        UUID cartItemId,
        UUID productId,
        UUID sellerId,
        UUID shopId,
        String productName,
        String imageUrl,
        int quantity,
        CartItemStatus status,
        BigDecimal effectivePrice,
        boolean inStock,
        int availableQuantity,
        String unavailableReason
) {
}
