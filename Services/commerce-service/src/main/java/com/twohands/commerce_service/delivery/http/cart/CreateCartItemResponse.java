package com.twohands.commerce_service.delivery.http.cart;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.cart.CartItemStatus;

import java.util.UUID;

public record CreateCartItemResponse(
        @JsonProperty("cart_item_id") UUID cartItemId,
        @JsonProperty("product_id") UUID productId,
        @JsonProperty("seller_id") UUID sellerId,
        @JsonProperty("quantity") int quantity,
        @JsonProperty("status") CartItemStatus status
) {
}
