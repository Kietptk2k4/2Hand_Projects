package com.twohands.commerce_service.domain.order;

import java.time.Instant;
import java.util.UUID;

public record BuyerOrderCancellationResult(
        BuyerOrderCancelOutcome outcome,
        UUID orderId,
        Instant cancelledAt
) {
}
