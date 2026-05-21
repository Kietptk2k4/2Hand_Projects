package com.twohands.commerce_service.application.cart.createcart;

import com.twohands.commerce_service.domain.cart.CartItemStatus;

import java.util.UUID;

public record CreateCartItemResult(
        UUID cartItemId,
        UUID productId,
        UUID sellerId,
        int quantity,
        CartItemStatus status
) {
}
