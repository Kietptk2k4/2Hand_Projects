package com.twohands.commerce_service.application.shipping.calculateshippingfee;

import com.twohands.commerce_service.domain.shipping.ShipmentType;

import java.util.List;
import java.util.UUID;

public record CalculateShippingFeeCommand(
        UUID userId,
        List<UUID> cartItemIds,
        UUID addressId,
        ShipmentType shipmentType
) {
}
