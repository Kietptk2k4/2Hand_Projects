package com.twohands.commerce_service.delivery.http.shipping;

import com.twohands.commerce_service.domain.shipping.ShipmentType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CalculateShippingFeeRequest(
        @NotEmpty(message = "At least one cart item is required")
        List<@NotNull UUID> cartItemIds,
        @NotNull UUID addressId,
        ShipmentType shipmentType
) {
}
