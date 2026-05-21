package com.twohands.commerce_service.application.order.completeorder;

import com.twohands.commerce_service.domain.order.OrderStatus;

import java.time.Instant;
import java.util.UUID;

public record CompleteOrderResponse(
        UUID orderId,
        OrderStatus orderStatus,
        Instant completedAt,
        boolean alreadyCompleted
) {
}
