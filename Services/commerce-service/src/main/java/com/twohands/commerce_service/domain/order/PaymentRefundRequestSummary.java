package com.twohands.commerce_service.domain.order;

import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentRefundRequestStatus;
import com.twohands.commerce_service.domain.payment.PaymentRefundRequestedBy;
import com.twohands.commerce_service.domain.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentRefundRequestSummary(
        UUID refundRequestId,
        PaymentRefundRequestStatus status,
        PaymentRefundRequestedBy requestedBy,
        BigDecimal amount,
        String reason,
        Instant requestedAt
) {
}
