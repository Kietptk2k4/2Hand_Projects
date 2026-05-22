package com.twohands.commerce_service.domain.cart;

import java.util.UUID;

public record InvalidCartItem(
        UUID cartItemId,
        String reason,
        CartItemStatus currentStatus
) {
}
