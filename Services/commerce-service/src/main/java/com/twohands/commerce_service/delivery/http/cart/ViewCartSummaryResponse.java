package com.twohands.commerce_service.delivery.http.cart;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public record ViewCartSummaryResponse(
        @JsonProperty("active_item_count") int activeItemCount,
        @JsonProperty("invalid_item_count") int invalidItemCount,
        BigDecimal subtotal,
        @JsonProperty("can_checkout") boolean canCheckout,
        List<String> warnings
) {
}
