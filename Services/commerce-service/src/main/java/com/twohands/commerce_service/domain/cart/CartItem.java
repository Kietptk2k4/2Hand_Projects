package com.twohands.commerce_service.domain.cart;

import java.time.Instant;
import java.util.UUID;

public record CartItem(
        UUID id,
        UUID cartId,
        UUID productId,
        UUID sellerId,
        int quantity,
        CartItemStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public CartItem withQuantityAndStatus(int quantity, CartItemStatus status, Instant updatedAt) {
        return new CartItem(id, cartId, productId, sellerId, quantity, status, createdAt, updatedAt);
    }

    public CartItem withQuantityStatusAndSeller(
            int quantity,
            CartItemStatus status,
            UUID sellerId,
            Instant updatedAt
    ) {
        return new CartItem(id, cartId, productId, sellerId, quantity, status, createdAt, updatedAt);
    }
}
