package com.twohands.commerce_service.delivery.http.order;

import com.twohands.commerce_service.domain.order.OrderStatus;

import java.time.Instant;
import java.util.UUID;

public record CancelOrderResponse(
        UUID orderId,
        OrderStatus status,
        Instant cancelledAt
) {
}
