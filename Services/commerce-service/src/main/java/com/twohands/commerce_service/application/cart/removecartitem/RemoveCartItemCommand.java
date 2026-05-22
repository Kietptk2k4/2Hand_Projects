package com.twohands.commerce_service.application.cart.removecartitem;

import java.util.UUID;

public record RemoveCartItemCommand(UUID userId, UUID cartItemId) {
}
