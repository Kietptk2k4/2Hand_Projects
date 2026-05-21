package com.twohands.commerce_service.application.order.cancelorder;

import java.util.UUID;

public record CancelOrderCommand(
        UUID buyerId,
        UUID orderId,
        String reason
) {
}
