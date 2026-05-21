package com.twohands.commerce_service.application.checkout.calculateordertotal;

import com.twohands.commerce_service.domain.shipping.ShipmentType;

import java.util.List;
import java.util.UUID;

public record CalculateOrderTotalCommand(
        UUID userId,
        List<UUID> cartItemIds,
        UUID addressId,
        ShipmentType shipmentType
) {
}
