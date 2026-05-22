package com.twohands.commerce_service.domain.order;

import java.util.UUID;

public record SellerOrderItemLine(
        UUID orderItemId,
        UUID orderId,
        UUID sellerId,
        String status,
        String productNameSnapshot,
        int quantity
) {
    public boolean isPending() {
        return OrderItemStatus.PENDING.name().equals(status);
    }

    public boolean isProcessing() {
        return OrderItemStatus.PROCESSING.name().equals(status);
    }

    public boolean canMarkProcessing() {
        return isPending() || isProcessing();
    }
}
