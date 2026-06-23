package com.twohands.commerce_service.delivery.http.checkout;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.payment.PaymentMethod;
import com.twohands.commerce_service.domain.shipping.ShipmentType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CheckoutFromCartRequest(
        @JsonProperty("cart_item_ids")
        @NotEmpty(message = "At least one cart item is required")
        List<@NotNull UUID> cartItemIds,
        @JsonProperty("address_id")
        @NotNull UUID addressId,
        @JsonProperty("payment_method")
        @NotNull PaymentMethod paymentMethod,
        @JsonProperty("shipment_type")
        ShipmentType shipmentType,
        @JsonProperty("idempotency_key")
        String idempotencyKey,
        @JsonProperty("frontend_return_url")
        String frontendReturnUrl,
        @JsonProperty("vnpay_return_url")
        String vnpayReturnUrl
) {
}
