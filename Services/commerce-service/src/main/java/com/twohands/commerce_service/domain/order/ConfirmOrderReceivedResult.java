package com.twohands.commerce_service.domain.order;

import com.twohands.commerce_service.domain.payment.PaymentStatus;

import java.util.UUID;

public record ConfirmOrderReceivedResult(
        UUID orderId,
        OrderStatus orderStatus,
        PaymentStatus paymentStatus,
        int itemsCompleted,
        boolean paymentMarkedPaid,
        boolean orderCompleted,
        boolean alreadyCompleted
) {
}
