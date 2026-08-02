package com.twohands.commerce_service.delivery.http.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public record ViewFlashSaleProductsResponse(
        List<ProductCardResponse> items,
        @JsonProperty("slot_start") Instant slotStart,
        @JsonProperty("slot_end") Instant slotEnd
) {
}
