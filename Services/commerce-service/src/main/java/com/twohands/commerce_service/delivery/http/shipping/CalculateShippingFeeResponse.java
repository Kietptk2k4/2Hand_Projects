package com.twohands.commerce_service.delivery.http.shipping;

import java.math.BigDecimal;
import java.util.List;

public record CalculateShippingFeeResponse(
        List<SellerShippingFeeGroupResponse> sellerGroups,
        BigDecimal totalShippingFee
) {
}
