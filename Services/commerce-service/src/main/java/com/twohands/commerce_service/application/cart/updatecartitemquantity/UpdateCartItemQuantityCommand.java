package com.twohands.commerce_service.application.cart.updatecartitemquantity;

import java.util.UUID;

public record UpdateCartItemQuantityCommand(
        UUID userId,
        UUID cartItemId,
        int quantity
) {
}
