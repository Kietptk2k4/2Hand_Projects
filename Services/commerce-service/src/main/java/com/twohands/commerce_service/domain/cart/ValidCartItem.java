package com.twohands.commerce_service.domain.cart;

import java.util.UUID;

public record ValidCartItem(
        UUID cartItemId,
        CartItemStatus currentStatus
) {
}
