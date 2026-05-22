package com.twohands.commerce_service.application.shipment.updatesellershipment;

import java.util.UUID;

public record UpdateSellerShipmentCommand(
        UUID sellerId,
        UUID shipmentId,
        String status,
        String trackingNumber
) {
}
