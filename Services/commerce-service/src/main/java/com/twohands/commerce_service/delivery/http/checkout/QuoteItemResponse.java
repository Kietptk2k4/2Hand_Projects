package com.twohands.commerce_service.delivery.http.checkout;

import java.math.BigDecimal;
import java.util.UUID;

public record QuoteItemResponse(
        UUID cartItemId,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal itemTotal,
        BigDecimal shippingFeeAllocated
) {
}
