package com.twohands.commerce_service.delivery.http.checkout;

import java.math.BigDecimal;
import java.util.List;

public record CalculateOrderTotalResponse(
        List<QuoteItemResponse> items,
        BigDecimal totalAmount,
        BigDecimal shippingFee,
        BigDecimal finalAmount,
        List<SellerShippingGroupResponse> sellerShippingGroups
) {
}
