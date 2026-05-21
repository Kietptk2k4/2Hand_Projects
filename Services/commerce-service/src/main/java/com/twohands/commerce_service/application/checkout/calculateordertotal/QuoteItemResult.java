package com.twohands.commerce_service.application.checkout.calculateordertotal;

import java.math.BigDecimal;
import java.util.UUID;

public record QuoteItemResult(
        UUID cartItemId,
        UUID productId,
        UUID sellerId,
        UUID shopId,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal itemTotal,
        BigDecimal shippingFeeAllocated
) {
}
