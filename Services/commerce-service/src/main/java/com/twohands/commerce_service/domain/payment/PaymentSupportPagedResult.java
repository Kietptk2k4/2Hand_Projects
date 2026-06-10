package com.twohands.commerce_service.domain.payment;

import java.util.List;

public record PaymentSupportPagedResult(
        List<PaymentSupportListEntry> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
