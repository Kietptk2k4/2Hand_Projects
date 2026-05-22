package com.twohands.commerce_service.domain.payment;

import java.time.Instant;

public record PaymentStatusHistoryEntry(
        PaymentStatus oldStatus,
        PaymentStatus newStatus,
        Instant occurredAt
) {
}
