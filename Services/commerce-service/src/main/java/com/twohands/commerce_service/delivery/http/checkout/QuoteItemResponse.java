package com.twohands.commerce_service.delivery.http.checkout;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.UUID;

public record QuoteItemResponse(
        @JsonProperty("cart_item_id")
        UUID cartItemId,
        @JsonProperty("unit_price")
        BigDecimal unitPrice,
        int quantity,
        @JsonProperty("item_total")
        BigDecimal itemTotal,
        @JsonProperty("shipping_fee_allocated")
        BigDecimal shippingFeeAllocated
) {
}
