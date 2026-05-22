package com.twohands.commerce_service.domain.cart;

import java.util.UUID;

public record CartItemSyncCandidate(
        UUID cartItemId,
        UUID cartId,
        UUID productId,
        UUID sellerId,
        int quantity,
        CartItemStatus currentStatus
) {
}
