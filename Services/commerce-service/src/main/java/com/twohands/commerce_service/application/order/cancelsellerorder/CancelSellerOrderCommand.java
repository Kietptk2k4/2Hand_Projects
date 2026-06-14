package com.twohands.commerce_service.application.order.cancelsellerorder;

import java.util.UUID;

public record CancelSellerOrderCommand(
        UUID sellerId,
        UUID orderId,
        String reason
) {
}
