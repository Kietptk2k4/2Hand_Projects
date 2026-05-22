package com.twohands.commerce_service.delivery.http.cart;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record InvalidCartItemResponse(
        @JsonProperty("cart_item_id")
        UUID cartItemId,
        String reason,
        @JsonProperty("current_status")
        String currentStatus
) {
}
