package com.twohands.commerce_service.domain.order;

import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;

import java.time.Instant;
import java.util.Optional;

public record OrderSupportListSearchCriteria(
        Optional<OrderStatus> status,
        Optional<PaymentMethod> paymentMethod,
        Optional<PaymentStatus> paymentStatus,
        Optional<String> searchQuery,
        Instant from,
        Instant to,
        OrderSupportListSortField sortField
) {
}
