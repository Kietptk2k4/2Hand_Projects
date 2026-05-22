package com.twohands.commerce_service.domain.shipment;

import java.time.Instant;
import java.util.UUID;

public record ViewShippingAddressSnapshotResult(
        UUID shipmentId,
        UUID snapshotId,
        ShipmentAddressSnapshot addressSnapshot,
        ShipmentAccessRole accessedAs,
        Instant createdAt
) {
}
