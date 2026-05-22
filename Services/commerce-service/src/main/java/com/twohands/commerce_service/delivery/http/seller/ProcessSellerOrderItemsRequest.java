package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record ProcessSellerOrderItemsRequest(
        @NotEmpty
        @JsonProperty("order_item_ids")
        List<UUID> orderItemIds
) {
}
