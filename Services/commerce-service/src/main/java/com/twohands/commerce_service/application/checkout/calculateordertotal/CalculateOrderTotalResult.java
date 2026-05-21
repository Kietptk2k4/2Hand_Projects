package com.twohands.commerce_service.application.checkout.calculateordertotal;

import java.math.BigDecimal;
import java.util.List;

public record CalculateOrderTotalResult(
        List<QuoteItemResult> items,
        BigDecimal totalAmount,
        BigDecimal shippingFee,
        BigDecimal finalAmount,
        List<SellerShippingGroupResult> sellerShippingGroups
) {
}
