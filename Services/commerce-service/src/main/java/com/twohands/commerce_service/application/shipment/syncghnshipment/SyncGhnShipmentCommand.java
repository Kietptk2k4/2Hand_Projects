package com.twohands.commerce_service.application.shipment.syncghnshipment;

import java.util.UUID;

public record SyncGhnShipmentCommand(
        UUID shipmentId,
        UUID userId,
        boolean force
) {
}
