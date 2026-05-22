package com.twohands.commerce_service.domain.order;

import java.time.Instant;
import java.util.UUID;

public record OrderItemTrackingLine(
        UUID orderItemId,
        UUID productId,
        UUID sellerId,
        String productName,
        int quantity,
        OrderItemStatus status,
        UUID shipmentId,
        Instant completedAt
) {
}
