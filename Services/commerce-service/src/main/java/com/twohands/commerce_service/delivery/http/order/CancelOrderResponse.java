package com.twohands.commerce_service.delivery.http.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.order.OrderStatus;

import java.time.Instant;
import java.util.UUID;

public record CancelOrderResponse(
        @JsonProperty("order_id") UUID orderId,
        OrderStatus status,
        @JsonProperty("cancelled_at") Instant cancelledAt,
        @JsonProperty("pending_refund") boolean pendingRefund,
        @JsonProperty("refund_request_id") UUID refundRequestId
) {
    public CancelOrderResponse(UUID orderId, OrderStatus status, Instant cancelledAt) {
        this(orderId, status, cancelledAt, false, null);
    }
}
