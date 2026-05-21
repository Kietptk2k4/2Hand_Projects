package com.twohands.commerce_service.application.shipping.calculateshippingfee;

import java.math.BigDecimal;
import java.util.List;

public record CalculateShippingFeeResult(
        List<SellerShippingFeeGroupResult> sellerGroups,
        BigDecimal totalShippingFee
) {
}
