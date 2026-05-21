package com.twohands.commerce_service.domain.order;

import java.time.Instant;
import java.util.UUID;

public record CompleteOrderResult(
        CompleteOrderOutcome outcome,
        UUID orderId,
        Instant completedAt
) {
    public boolean orderCompleted() {
        return outcome == CompleteOrderOutcome.COMPLETED;
    }

    public boolean idempotentSuccess() {
        return outcome == CompleteOrderOutcome.COMPLETED || outcome == CompleteOrderOutcome.ALREADY_COMPLETED;
    }
}
