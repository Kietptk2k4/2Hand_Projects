package com.twohands.commerce_service.domain.checkout;

import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.shipping.ShipmentType;

import java.util.List;
import java.util.UUID;

public record CheckoutFromCartRequest(
        UUID buyerId,
        List<UUID> cartItemIds,
        UUID addressId,
        PaymentMethod paymentMethod,
        ShipmentType shipmentType,
        String idempotencyKey
) {
}
