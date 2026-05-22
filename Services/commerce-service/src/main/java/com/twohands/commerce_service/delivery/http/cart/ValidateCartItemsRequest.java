package com.twohands.commerce_service.delivery.http.cart;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public record ValidateCartItemsRequest(
        @JsonProperty("cart_item_ids")
        List<UUID> cartItemIds
) {
}
