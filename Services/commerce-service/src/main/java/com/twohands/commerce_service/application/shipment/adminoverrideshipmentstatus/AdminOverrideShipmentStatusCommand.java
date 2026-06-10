package com.twohands.commerce_service.application.shipment.adminoverrideshipmentstatus;

import java.util.UUID;

public record AdminOverrideShipmentStatusCommand(
        UUID shipmentId,
        String status,
        String reason,
        boolean force
) {
}
