package com.twohands.commerce_service.domain.order;

import com.twohands.commerce_service.domain.payment.PaymentRefundRequestedBy;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRefundRequestRepository {

    Optional<ActiveRefundRequestRow> findActiveByOrderId(UUID orderId);

    UUID createRequested(
            UUID paymentId,
            UUID orderId,
            PaymentRefundRequestedBy requestedBy,
            UUID requestedByUserId,
            java.math.BigDecimal amount,
            String reason,
            Instant now
    );

    Optional<PaymentRefundRequestSummary> findSummaryByOrderId(UUID orderId);

    record ActiveRefundRequestRow(
            UUID refundRequestId,
            UUID paymentId,
            UUID orderId,
            PaymentRefundRequestedBy requestedBy,
            java.math.BigDecimal amount,
            Instant requestedAt
    ) {
    }
}
