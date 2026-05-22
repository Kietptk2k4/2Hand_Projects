package com.twohands.commerce_service.domain.order;

import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.payment.PaymentStatusHistoryEntry;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ViewOrderDetailPaymentSummary(
        UUID paymentId,
        PaymentStatus status,
        PaymentMethod paymentMethod,
        BigDecimal amount,
        String currency,
        Instant paidAt,
        Instant expiredAt,
        Instant checkoutUrlExpiredAt,
        List<PaymentStatusHistoryEntry> timeline
) {
}
