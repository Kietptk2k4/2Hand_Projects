package com.twohands.commerce_service.delivery.http.checkout;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public record CalculateOrderTotalResponse(
        List<QuoteItemResponse> items,
        @JsonProperty("total_amount")
        BigDecimal totalAmount,
        @JsonProperty("shipping_fee")
        BigDecimal shippingFee,
        @JsonProperty("final_amount")
        BigDecimal finalAmount,
        @JsonProperty("seller_shipping_groups")
        List<SellerShippingGroupResponse> sellerShippingGroups
) {
}
