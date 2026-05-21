package com.twohands.commerce_service.application.cart.addproducttocart;

import com.twohands.commerce_service.domain.cart.CartItemStatus;

import java.util.UUID;

public record AddProductToCartResult(
        UUID cartId,
        UUID cartItemId,
        UUID productId,
        int quantity,
        CartItemStatus status,
        ProductSummaryResult product
) {
}
