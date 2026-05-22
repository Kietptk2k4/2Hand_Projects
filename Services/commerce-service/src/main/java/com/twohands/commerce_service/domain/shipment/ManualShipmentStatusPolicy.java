package com.twohands.commerce_service.domain.shipment;

import com.twohands.commerce_service.domain.order.OrderItemStatus;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class ManualShipmentStatusPolicy {

    private static final Map<ShipmentStatus, Set<ShipmentStatus>> ALLOWED_TRANSITIONS = Map.of(
            ShipmentStatus.PENDING, Set.of(ShipmentStatus.READY_TO_SHIP),
            ShipmentStatus.READY_TO_SHIP, Set.of(ShipmentStatus.SHIPPED),
            ShipmentStatus.SHIPPED, Set.of(ShipmentStatus.DELIVERED, ShipmentStatus.FAILED)
    );

    private ManualShipmentStatusPolicy() {
    }

    public static boolean isEditableCarrier(ShipmentCarrier carrier) {
        return carrier == ShipmentCarrier.MANUAL || carrier == ShipmentCarrier.SELF_DELIVERY;
    }

    public static boolean canTransition(ShipmentStatus current, ShipmentStatus target) {
        if (current == target) {
            return true;
        }
        return ALLOWED_TRANSITIONS.getOrDefault(current, Set.of()).contains(target);
    }

    public static boolean canEditTracking(ShipmentStatus status) {
        return status != ShipmentStatus.DELIVERED
                && status != ShipmentStatus.CANCELLED
                && status != ShipmentStatus.FAILED
                && status != ShipmentStatus.RETURNED;
    }

    public static Optional<OrderItemStatus> orderItemStatusForShipmentStatus(ShipmentStatus shipmentStatus) {
        return switch (shipmentStatus) {
            case SHIPPED -> Optional.of(OrderItemStatus.SHIPPED);
            case DELIVERED -> Optional.of(OrderItemStatus.DELIVERED);
            case FAILED -> Optional.of(OrderItemStatus.FAILED);
            case RETURNED -> Optional.of(OrderItemStatus.RETURNED);
            default -> Optional.empty();
        };
    }
}
