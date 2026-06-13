package com.twohands.commerce_service.domain.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ViewOrderDetailItem(
        UUID orderItemId,
        UUID productId,
        UUID sellerId,
        UUID shipmentId,
        int quantity,
        OrderItemStatus status,
        BigDecimal unitPriceSnapshot,
        BigDecimal finalPrice,
        String skuSnapshot,
        String productNameSnapshot,
        String imageSnapshot,
        String attributesSnapshot,
        String shopNameSnapshot,
        BigDecimal shippingFeeAllocated,
        Instant completedAt,
        UUID reviewId
) {
}
