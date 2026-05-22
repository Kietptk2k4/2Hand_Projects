package com.twohands.commerce_service.domain.shipment;

import com.twohands.commerce_service.domain.order.OrderItemStatus;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class GhnShipmentStatusPolicy {

    private static final Map<ShipmentStatus, Set<ShipmentStatus>> ALLOWED_TRANSITIONS = Map.of(
            ShipmentStatus.PENDING, Set.of(
                    ShipmentStatus.PICKING_UP,
                    ShipmentStatus.READY_TO_SHIP,
                    ShipmentStatus.SHIPPED,
                    ShipmentStatus.CANCELLED,
                    ShipmentStatus.FAILED
            ),
            ShipmentStatus.PICKING_UP, Set.of(
                    ShipmentStatus.READY_TO_SHIP,
                    ShipmentStatus.SHIPPED,
                    ShipmentStatus.CANCELLED,
                    ShipmentStatus.FAILED
            ),
            ShipmentStatus.READY_TO_SHIP, Set.of(
                    ShipmentStatus.SHIPPED,
                    ShipmentStatus.CANCELLED,
                    ShipmentStatus.FAILED
            ),
            ShipmentStatus.SHIPPED, Set.of(
                    ShipmentStatus.DELIVERED,
                    ShipmentStatus.FAILED,
                    ShipmentStatus.RETURNED
            ),
            ShipmentStatus.DELIVERED, Set.of(ShipmentStatus.RETURNED),
            ShipmentStatus.FAILED, Set.of(),
            ShipmentStatus.CANCELLED, Set.of(),
            ShipmentStatus.RETURNED, Set.of()
    );

    private GhnShipmentStatusPolicy() {
    }

    public static boolean canTransition(ShipmentStatus current, ShipmentStatus target) {
        if (current == target) {
            return true;
        }
        return ALLOWED_TRANSITIONS.getOrDefault(current, Set.of()).contains(target);
    }

    public static Optional<OrderItemStatus> orderItemStatusForShipmentStatus(ShipmentStatus shipmentStatus) {
        return switch (shipmentStatus) {
            case PICKING_UP, READY_TO_SHIP, SHIPPED -> Optional.of(OrderItemStatus.SHIPPED);
            case DELIVERED -> Optional.of(OrderItemStatus.DELIVERED);
            case FAILED -> Optional.of(OrderItemStatus.FAILED);
            case RETURNED -> Optional.of(OrderItemStatus.RETURNED);
            default -> Optional.empty();
        };
    }
}
