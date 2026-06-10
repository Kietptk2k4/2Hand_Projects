package com.twohands.commerce_service.domain.shipment;

import com.twohands.commerce_service.domain.order.OrderItemStatus;

import java.util.Optional;
import java.util.Set;

public final class AdminShipmentStatusOverridePolicy {

    public static final String RAW_STATUS = "admin_override";
    public static final int REASON_MIN_LENGTH = 10;
    public static final int REASON_MAX_LENGTH = 500;

    private static final Set<ShipmentStatus> TERMINAL_STATUSES = Set.of(
            ShipmentStatus.DELIVERED,
            ShipmentStatus.CANCELLED,
            ShipmentStatus.RETURNED,
            ShipmentStatus.FAILED
    );

    private AdminShipmentStatusOverridePolicy() {
    }

    public static boolean isTerminal(ShipmentStatus status) {
        return TERMINAL_STATUSES.contains(status);
    }

    public static boolean canTransition(
            ShipmentCarrier carrier,
            ShipmentStatus current,
            ShipmentStatus target,
            boolean force
    ) {
        if (current == target) {
            return true;
        }
        if (!force && isTerminal(current)) {
            return false;
        }
        if (force) {
            return true;
        }
        return switch (carrier) {
            case GHN -> GhnShipmentStatusPolicy.canTransition(current, target);
            case MANUAL, SELF_DELIVERY -> ManualShipmentStatusPolicy.canTransition(current, target);
        };
    }

    public static Optional<OrderItemStatus> orderItemStatusForCarrier(
            ShipmentCarrier carrier,
            ShipmentStatus shipmentStatus
    ) {
        return switch (carrier) {
            case GHN -> GhnShipmentStatusPolicy.orderItemStatusForShipmentStatus(shipmentStatus);
            case MANUAL, SELF_DELIVERY -> ManualShipmentStatusPolicy.orderItemStatusForShipmentStatus(shipmentStatus);
        };
    }
}
