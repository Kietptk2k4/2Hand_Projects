package com.twohands.commerce_service.delivery.http.shipping;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public record CalculateShippingFeeResponse(
        @JsonProperty("seller_groups")
        List<SellerShippingFeeGroupResponse> sellerGroups,
        @JsonProperty("total_shipping_fee")
        BigDecimal totalShippingFee
) {
}
