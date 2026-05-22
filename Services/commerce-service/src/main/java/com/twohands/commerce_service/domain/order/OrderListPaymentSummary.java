package com.twohands.commerce_service.domain.order;

import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderListPaymentSummary(
        UUID paymentId,
        PaymentStatus status,
        PaymentMethod paymentMethod,
        BigDecimal amount,
        String currency
) {
}
