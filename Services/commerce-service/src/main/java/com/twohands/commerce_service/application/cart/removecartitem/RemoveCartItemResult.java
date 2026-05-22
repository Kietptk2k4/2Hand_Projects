package com.twohands.commerce_service.application.cart.removecartitem;

import com.twohands.commerce_service.domain.cart.CartItemStatus;

import java.time.Instant;
import java.util.UUID;

public record RemoveCartItemResult(
        UUID cartId,
        UUID cartItemId,
        UUID productId,
        CartItemStatus status,
        Instant removedAt,
        int activeItemCount,
        boolean alreadyRemoved
) {
}
