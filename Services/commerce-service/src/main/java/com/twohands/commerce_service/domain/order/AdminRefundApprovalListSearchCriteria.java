package com.twohands.commerce_service.domain.order;

import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentRefundRequestStatus;
import com.twohands.commerce_service.domain.payment.PaymentRefundRequestedBy;

import java.time.Instant;
import java.util.Optional;

public record AdminRefundApprovalListSearchCriteria(
        Optional<PaymentRefundRequestStatus> status,
        Optional<String> searchQuery,
        Optional<PaymentRefundRequestedBy> requestedBy,
        Optional<PaymentMethod> paymentMethod,
        Optional<Instant> from,
        Optional<Instant> to
) {
}
