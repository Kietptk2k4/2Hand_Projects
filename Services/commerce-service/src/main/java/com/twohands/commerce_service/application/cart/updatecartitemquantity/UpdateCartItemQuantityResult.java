package com.twohands.commerce_service.application.cart.updatecartitemquantity;

import com.twohands.commerce_service.application.cart.addproducttocart.ProductSummaryResult;
import com.twohands.commerce_service.domain.cart.CartItemStatus;

import java.util.UUID;

public record UpdateCartItemQuantityResult(
        UUID cartId,
        UUID cartItemId,
        UUID productId,
        int quantity,
        CartItemStatus status,
        ProductSummaryResult product,
        int activeItemCount
) {
}
