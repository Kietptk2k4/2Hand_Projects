package com.twohands.commerce_service.application.order.cancelorder;

import com.twohands.commerce_service.domain.order.OrderStatus;

import java.time.Instant;
import java.util.UUID;

public record CancelOrderResult(
        UUID orderId,
        OrderStatus status,
        Instant cancelledAt,
        boolean alreadyCancelled,
        boolean pendingRefund,
        UUID refundRequestId
) {
    public CancelOrderResult(UUID orderId, OrderStatus status, Instant cancelledAt, boolean alreadyCancelled) {
        this(orderId, status, cancelledAt, alreadyCancelled, false, null);
    }
}
