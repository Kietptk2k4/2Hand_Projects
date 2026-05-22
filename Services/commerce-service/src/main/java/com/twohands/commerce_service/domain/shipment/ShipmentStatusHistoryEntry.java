package com.twohands.commerce_service.domain.shipment;

import java.time.Instant;

public record ShipmentStatusHistoryEntry(
        ShipmentStatus oldStatus,
        ShipmentStatus newStatus,
        String rawStatus,
        Instant occurredAt
) {
}
