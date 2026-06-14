package com.twohands.commerce_service.domain.shipment;

import java.util.EnumSet;
import java.util.Set;

public final class SellerShipmentCancellationPolicy {

    private static final Set<ShipmentStatus> GHN_CANCELLABLE = EnumSet.of(
            ShipmentStatus.PENDING,
            ShipmentStatus.PICKING_UP,
            ShipmentStatus.READY_TO_SHIP
    );

    private static final Set<ShipmentStatus> MANUAL_CANCELLABLE = EnumSet.of(
            ShipmentStatus.PENDING,
            ShipmentStatus.READY_TO_SHIP
    );

    private SellerShipmentCancellationPolicy() {
    }

    public static boolean canCancel(ShipmentCarrier carrier, ShipmentStatus status) {
        if (carrier == null || status == null) {
            return false;
        }
        return switch (carrier) {
            case GHN -> GHN_CANCELLABLE.contains(status);
            case MANUAL, SELF_DELIVERY -> MANUAL_CANCELLABLE.contains(status);
        };
    }
}
