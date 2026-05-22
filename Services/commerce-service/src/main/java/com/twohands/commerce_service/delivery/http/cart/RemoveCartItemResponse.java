package com.twohands.commerce_service.delivery.http.cart;

import com.twohands.commerce_service.domain.cart.CartItemStatus;

import java.time.Instant;
import java.util.UUID;

public record RemoveCartItemResponse(
        UUID cartId,
        UUID cartItemId,
        UUID productId,
        CartItemStatus status,
        Instant removedAt,
        CartSummaryResponse cartSummary,
        boolean alreadyRemoved
) {
}
