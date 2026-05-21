package com.twohands.commerce_service.delivery.http.checkout;

import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.shipping.ShipmentType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CheckoutFromCartRequest(
        @NotEmpty(message = "At least one cart item is required")
        List<@NotNull UUID> cartItemIds,
        @NotNull UUID addressId,
        @NotNull PaymentMethod paymentMethod,
        ShipmentType shipmentType,
        String idempotencyKey
) {
}
