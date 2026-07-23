package com.twohands.commerce_service.application.refund.listadminrefundapprovals;

import com.twohands.commerce_service.domain.payment.PaymentRefundRequestStatus;

import java.util.Optional;

public record ListAdminRefundApprovalsCommand(
        Optional<PaymentRefundRequestStatus> status,
        Optional<String> searchQuery,
        Optional<String> requestedBy,
        Optional<String> paymentMethod,
        Optional<String> from,
        Optional<String> to,
        Integer page,
        Integer limit
) {
}
