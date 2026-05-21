package com.twohands.commerce_service.application.cart.addproducttocart;

import java.util.UUID;

public record AddProductToCartCommand(UUID userId, UUID productId, int quantity) {
}
