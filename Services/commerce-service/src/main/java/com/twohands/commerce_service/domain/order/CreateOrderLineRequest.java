package com.twohands.commerce_service.domain.order;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrderLineRequest(
        UUID productId,
        UUID sellerId,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal,
        String productName,
        String shopName,
        String sku,
        String imageUrl,
        String attributesJson,
        BigDecimal shippingFeeAllocated
) {
}
