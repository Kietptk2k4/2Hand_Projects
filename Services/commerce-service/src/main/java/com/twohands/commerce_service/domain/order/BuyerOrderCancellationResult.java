package com.twohands.commerce_service.domain.order;

import java.time.Instant;
import java.util.UUID;

public record BuyerOrderCancellationResult(
        BuyerOrderCancelOutcome outcome,
        UUID orderId,
        Instant cancelledAt,
        UUID refundRequestId
) {
    public BuyerOrderCancellationResult(BuyerOrderCancelOutcome outcome, UUID orderId, Instant cancelledAt) {
        this(outcome, orderId, cancelledAt, null);
    }
}
