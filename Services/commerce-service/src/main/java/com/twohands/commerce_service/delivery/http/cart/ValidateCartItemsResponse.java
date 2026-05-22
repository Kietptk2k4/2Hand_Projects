package com.twohands.commerce_service.delivery.http.cart;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ValidateCartItemsResponse(
        @JsonProperty("valid_items")
        List<ValidCartItemResponse> validItems,
        @JsonProperty("invalid_items")
        List<InvalidCartItemResponse> invalidItems,
        @JsonProperty("can_checkout")
        boolean canCheckout
) {
}
