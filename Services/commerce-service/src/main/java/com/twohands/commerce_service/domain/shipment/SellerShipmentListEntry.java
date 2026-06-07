package com.twohands.commerce_service.domain.shipment;

import com.twohands.commerce_service.domain.shipping.ShipmentType;

import java.time.Instant;
import java.util.UUID;

public record SellerShipmentListEntry(
        UUID shipmentId,
        UUID orderId,
        ShipmentCarrier carrier,
        ShipmentType shipmentType,
        ShipmentStatus status,
        String trackingNumber,
        String ghnOrderCode,
        String deliveryAddressSummary,
        Instant createdAt,
        Instant updatedAt,
        int orderItemCount
) {
}
