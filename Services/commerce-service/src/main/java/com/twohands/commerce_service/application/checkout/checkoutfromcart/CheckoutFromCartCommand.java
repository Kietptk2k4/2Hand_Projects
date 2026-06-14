package com.twohands.commerce_service.application.checkout.checkoutfromcart;

import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.shipping.ShipmentType;

import java.util.List;
import java.util.UUID;

public record CheckoutFromCartCommand(
        UUID buyerId,
        List<UUID> cartItemIds,
        UUID addressId,
        PaymentMethod paymentMethod,
        ShipmentType shipmentType,
        String idempotencyKey,
        String clientIp
) {
}
