package com.twohands.commerce_service.domain.order;

public final class OrderShipmentCancellationGuard {

    private OrderShipmentCancellationGuard() {
    }

    /**
     * Blocks cancellation when any shipment has reached in-transit or terminal delivery states.
     */
    public static boolean isBlockingShipmentStatus(String shipmentStatus) {
        if (shipmentStatus == null || shipmentStatus.isBlank()) {
            return false;
        }
        return switch (shipmentStatus.trim().toUpperCase()) {
            case "SHIPPED", "DELIVERED", "RETURNED" -> true;
            default -> false;
        };
    }
}
