package com.twohands.commerce_service.domain.payment;

import java.time.Instant;
import java.util.UUID;

public record PaymentSupportSearchCriteria(
        PaymentStatus status,
        PaymentMethod paymentMethod,
        UUID orderId,
        String searchQuery,
        PaymentSupportReconciliationStatus reconciliationStatus,
        Instant from,
        Instant to
) {
}
