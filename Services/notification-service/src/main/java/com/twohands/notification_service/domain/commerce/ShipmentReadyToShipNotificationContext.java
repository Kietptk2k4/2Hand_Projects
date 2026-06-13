package com.twohands.notification_service.domain.commerce;

import java.util.UUID;

public record ShipmentReadyToShipNotificationContext(
        UUID buyerId,
        String shipmentId,
        String orderId,
        String trackingCode
) {
}
