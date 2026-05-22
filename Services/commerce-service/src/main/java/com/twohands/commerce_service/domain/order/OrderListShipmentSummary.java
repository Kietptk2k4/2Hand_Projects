package com.twohands.commerce_service.domain.order;

import com.twohands.commerce_service.domain.shipment.ShipmentStatus;

import java.util.List;

public record OrderListShipmentSummary(
        int shipmentCount,
        List<ShipmentStatus> statuses
) {
    public static OrderListShipmentSummary empty() {
        return new OrderListShipmentSummary(0, List.of());
    }
}
