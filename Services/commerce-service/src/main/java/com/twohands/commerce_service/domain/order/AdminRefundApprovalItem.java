package com.twohands.commerce_service.domain.order;

import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentRefundRequestStatus;
import com.twohands.commerce_service.domain.payment.PaymentRefundRequestedBy;
import com.twohands.commerce_service.domain.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AdminRefundApprovalItem(
        UUID id,
        UUID paymentId,
        UUID orderId,
        UUID buyerId,
        PaymentRefundRequestedBy requestedBy,
        UUID requestedByUserId,
        PaymentRefundRequestStatus status,
        BigDecimal amount,
        String reason,
        String adminNote,
        PaymentMethod paymentMethod,
        PaymentStatus orderPaymentStatus,
        OrderStatus orderStatus,
        Instant requestedAt,
        Instant confirmedAt,
        Instant rejectedAt
) {
}
