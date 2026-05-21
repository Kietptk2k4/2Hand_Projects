package com.twohands.commerce_service.delivery.http.order;

import com.twohands.commerce_service.domain.order.OrderStatus;
import com.twohands.commerce_service.domain.payment.PaymentStatus;

import java.util.UUID;

public record ConfirmOrderReceivedResponse(
        UUID orderId,
        OrderStatus orderStatus,
        PaymentStatus paymentStatus,
        int itemsCompleted,
        boolean paymentMarkedPaid,
        boolean orderCompleted
) {
}
