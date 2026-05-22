package com.twohands.commerce_service.application.shipment.viewshippingaddresssnapshot;

import java.util.UUID;

public record ViewShippingAddressSnapshotCommand(
        UUID userId,
        UUID shipmentId
) {
}
