package com.twohands.commerce_service.application.refund.listadminrefundapprovals;

import com.twohands.commerce_service.domain.payment.PaymentRefundRequestStatus;

import java.util.Optional;

public record ListAdminRefundApprovalsCommand(
        Optional<PaymentRefundRequestStatus> status,
        Integer page,
        Integer limit
) {
}
