package com.twohands.commerce_service.delivery.http.cart;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.cart.CartItemStatus;

import java.util.UUID;

public record UpdateCartItemQuantityResponse(
        @JsonProperty("cart_id") UUID cartId,
        @JsonProperty("cart_item_id") UUID cartItemId,
        @JsonProperty("product_id") UUID productId,
        int quantity,
        CartItemStatus status,
        ProductSummaryResponse product,
        @JsonProperty("cart_summary") CartSummaryResponse cartSummary
) {
}
