package com.twohands.commerce_service.domain.payment;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderPaymentTracking(
        UUID paymentId,
        PaymentStatus status,
        PaymentMethod paymentMethod,
        Instant paidAt,
        Instant expiredAt,
        List<PaymentStatusHistoryEntry> timeline
) {
}
