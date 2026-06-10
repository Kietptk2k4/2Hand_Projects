package com.twohands.commerce_service.application.shipment.adminoverrideshipmentstatus;

import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;

import java.time.Instant;
import java.util.UUID;

public record AdminOverrideShipmentStatusResult(
        UUID shipmentId,
        UUID orderId,
        ShipmentCarrier carrier,
        ShipmentStatus previousStatus,
        ShipmentStatus currentStatus,
        int orderItemsUpdated,
        Instant occurredAt,
        boolean applied
) {
    public static AdminOverrideShipmentStatusResult unchanged(
            UUID shipmentId,
            UUID orderId,
            ShipmentCarrier carrier,
            ShipmentStatus status
    ) {
        return new AdminOverrideShipmentStatusResult(
                shipmentId,
                orderId,
                carrier,
                status,
                status,
                0,
                null,
                false
        );
    }
}
