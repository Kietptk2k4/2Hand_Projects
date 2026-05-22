package com.twohands.commerce_service.delivery.http.cart;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ValidCartItemResponse(
        @JsonProperty("cart_item_id")
        UUID cartItemId,
        @JsonProperty("current_status")
        String currentStatus
) {
}
