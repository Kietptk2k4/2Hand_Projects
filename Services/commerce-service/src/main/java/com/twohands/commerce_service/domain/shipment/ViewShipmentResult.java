package com.twohands.commerce_service.domain.shipment;

import java.util.List;

public record ViewShipmentResult(
        SellerShipmentRecord shipment,
        ShipmentAddressSnapshot addressSnapshot,
        List<ShipmentOrderItemSummary> orderItems,
        List<ShipmentStatusHistoryEntry> statusHistory,
        ShipmentAccessRole accessedAs
) {
}
