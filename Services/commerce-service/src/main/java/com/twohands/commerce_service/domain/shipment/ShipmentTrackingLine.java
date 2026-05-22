package com.twohands.commerce_service.domain.shipment;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ShipmentTrackingLine(
        UUID shipmentId,
        UUID sellerId,
        ShipmentStatus status,
        String carrier,
        String trackingNumber,
        Instant shippedAt,
        Instant deliveredAt,
        List<ShipmentStatusHistoryEntry> timeline
) {
}
