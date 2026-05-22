package com.twohands.commerce_service.domain.shipment;

import java.util.Optional;

public final class GhnShipmentStatusMapper {

    private GhnShipmentStatusMapper() {
    }

    public static Optional<ShipmentStatus> map(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return Optional.empty();
        }
        String normalized = rawStatus.trim().toLowerCase().replace('-', '_');

        if (normalized.contains("deliver") && normalized.contains("fail")) {
            return Optional.of(ShipmentStatus.FAILED);
        }
        if (normalized.contains("delivered") || normalized.equals("delivery_success")) {
            return Optional.of(ShipmentStatus.DELIVERED);
        }
        if (normalized.contains("return")) {
            return Optional.of(ShipmentStatus.RETURNED);
        }
        if (normalized.contains("cancel")) {
            return Optional.of(ShipmentStatus.CANCELLED);
        }
        if (normalized.contains("exception") || normalized.contains("fail")) {
            return Optional.of(ShipmentStatus.FAILED);
        }
        if (normalized.contains("transport") || normalized.contains("delivering")) {
            return Optional.of(ShipmentStatus.SHIPPED);
        }
        if (normalized.contains("ready_to_pick")
                || normalized.equals("picked")
                || normalized.contains("sorting")
                || normalized.contains("storing")) {
            return Optional.of(ShipmentStatus.READY_TO_SHIP);
        }
        if (normalized.contains("picking")) {
            return Optional.of(ShipmentStatus.PICKING_UP);
        }
        return Optional.empty();
    }
}
