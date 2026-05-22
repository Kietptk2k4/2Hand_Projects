package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ShipmentOrderItemSummaryResponse(
        @JsonProperty("order_item_id") UUID orderItemId,
        @JsonProperty("product_name_snapshot") String productNameSnapshot,
        int quantity,
        String status
) {
}
