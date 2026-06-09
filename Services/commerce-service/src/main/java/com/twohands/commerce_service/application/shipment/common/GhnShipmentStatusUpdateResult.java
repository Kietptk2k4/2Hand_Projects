package com.twohands.commerce_service.application.shipment.common;

import com.twohands.commerce_service.domain.shipment.ShipmentStatus;

import java.util.UUID;

public record GhnShipmentStatusUpdateResult(
        boolean applied,
        boolean unchanged,
        boolean ignoredTransition,
        boolean unmappedStatus,
        UUID shipmentId,
        ShipmentStatus previousStatus,
        ShipmentStatus newStatus
) {
    public static GhnShipmentStatusUpdateResult unmapped(UUID shipmentId, ShipmentStatus current) {
        return new GhnShipmentStatusUpdateResult(false, false, false, true, shipmentId, current, current);
    }

    public static GhnShipmentStatusUpdateResult unchanged(UUID shipmentId, ShipmentStatus status) {
        return new GhnShipmentStatusUpdateResult(false, true, false, false, shipmentId, status, status);
    }

    public static GhnShipmentStatusUpdateResult ignored(
            UUID shipmentId,
            ShipmentStatus current,
            ShipmentStatus attempted
    ) {
        return new GhnShipmentStatusUpdateResult(false, false, true, false, shipmentId, current, attempted);
    }

    public static GhnShipmentStatusUpdateResult updated(
            UUID shipmentId,
            ShipmentStatus previous,
            ShipmentStatus current
    ) {
        return new GhnShipmentStatusUpdateResult(true, false, false, false, shipmentId, previous, current);
    }
}
