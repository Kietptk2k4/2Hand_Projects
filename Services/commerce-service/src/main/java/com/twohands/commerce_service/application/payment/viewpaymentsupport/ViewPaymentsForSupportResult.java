package com.twohands.commerce_service.application.payment.viewpaymentsupport;

import com.twohands.commerce_service.domain.payment.PaymentSupportListEntry;

import java.util.List;

public record ViewPaymentsForSupportResult(
        int page,
        int size,
        long totalElements,
        int totalPages,
        List<PaymentSupportListEntry> payments
) {
}
