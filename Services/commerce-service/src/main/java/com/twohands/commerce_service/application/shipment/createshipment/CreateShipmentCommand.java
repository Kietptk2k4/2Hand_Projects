package com.twohands.commerce_service.application.shipment.createshipment;

import java.util.List;
import java.util.UUID;

public record CreateShipmentCommand(
        UUID sellerId,
        UUID orderId,
        List<UUID> orderItemIds,
        String carrier,
        String shipmentType,
        Integer weightGram,
        String trackingNumber
) {
}
