package com.twohands.commerce_service.application.shipment.viewshipment;

import java.util.UUID;

public record ViewShipmentCommand(
        UUID userId,
        UUID shipmentId
) {
}
