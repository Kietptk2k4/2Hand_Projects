package com.twohands.commerce_service.domain.payment;

import java.time.Instant;
import java.util.UUID;

public record HandlePaymentFailureResult(
        PaymentFailureOutcome outcome,
        UUID paymentId,
        UUID orderId,
        PaymentStatus terminalStatus,
        boolean inventoryReleased,
        Instant processedAt
) {
}
