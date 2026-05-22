package com.twohands.commerce_service.domain.order;

import com.twohands.commerce_service.domain.shipment.ShipmentCarrier;
import com.twohands.commerce_service.domain.shipment.ShipmentStatus;

import java.util.UUID;

public record SellerOrderListShipmentSummary(
        UUID shipmentId,
        ShipmentStatus status,
        ShipmentCarrier carrier,
        String trackingNumber,
        String deliveryAddressSummary
) {
    public static SellerOrderListShipmentSummary empty() {
        return new SellerOrderListShipmentSummary(null, null, null, null, null);
    }
}
