package com.twohands.commerce_service.application.shipment.common;

import com.twohands.commerce_service.domain.shipment.SellerShipmentRecord;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;

import java.util.UUID;

public record ShipmentStatusTransitionResult(
        boolean applied,
        UUID shipmentId,
        ShipmentStatus previousStatus,
        ShipmentStatus currentStatus,
        int orderItemsUpdated
) {
    public static ShipmentStatusTransitionResult unchanged(SellerShipmentRecord shipment) {
        return new ShipmentStatusTransitionResult(
                false,
                shipment.shipmentId(),
                shipment.status(),
                shipment.status(),
                0
        );
    }

    public static ShipmentStatusTransitionResult applied(
            SellerShipmentRecord shipment,
            ShipmentStatus newStatus,
            int orderItemsUpdated
    ) {
        return new ShipmentStatusTransitionResult(
                true,
                shipment.shipmentId(),
                shipment.status(),
                newStatus,
                orderItemsUpdated
        );
    }
}
