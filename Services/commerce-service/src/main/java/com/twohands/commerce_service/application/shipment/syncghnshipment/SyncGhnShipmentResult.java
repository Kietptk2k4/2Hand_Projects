package com.twohands.commerce_service.application.shipment.syncghnshipment;

import com.twohands.commerce_service.domain.shipment.ShipmentStatus;

import java.util.UUID;

public record SyncGhnShipmentResult(
        UUID shipmentId,
        boolean synced,
        boolean skipped,
        ShipmentStatus status,
        String message
) {
    public static SyncGhnShipmentResult skipped(UUID shipmentId, ShipmentStatus status, String message) {
        return new SyncGhnShipmentResult(shipmentId, false, true, status, message);
    }

    public static SyncGhnShipmentResult synced(UUID shipmentId, ShipmentStatus status) {
        return new SyncGhnShipmentResult(shipmentId, true, false, status, "Synced from GHN");
    }
}
