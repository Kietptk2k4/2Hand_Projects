package com.twohands.commerce_service.domain.payment;

import java.time.Instant;

public record PaymentWebhookSummary(
        String provider,
        String eventType,
        boolean signatureValid,
        boolean processed,
        Instant receivedAt
) {
}
