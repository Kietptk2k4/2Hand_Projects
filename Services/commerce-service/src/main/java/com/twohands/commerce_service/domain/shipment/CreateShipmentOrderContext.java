package com.twohands.commerce_service.domain.shipment;

import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.payment.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateShipmentOrderContext(
        UUID orderId,
        UUID buyerId,
        String orderStatus,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        BigDecimal orderFinalAmount
) {
}
