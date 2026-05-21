package com.twohands.commerce_service.domain.review;

import java.util.UUID;

public record ReviewableOrderItem(
        UUID orderItemId,
        UUID orderId,
        UUID buyerId,
        UUID sellerId,
        UUID productId,
        String status
) {
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }
}
