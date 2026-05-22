package com.twohands.commerce_service.application.shipment.trackshipment;

import java.util.UUID;

public record TrackShipmentCommand(
        UUID userId,
        UUID shipmentId
) {
}
