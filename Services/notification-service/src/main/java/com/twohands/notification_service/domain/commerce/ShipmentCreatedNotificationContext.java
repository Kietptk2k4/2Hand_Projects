package com.twohands.notification_service.domain.commerce;

import java.util.UUID;

public record ShipmentCreatedNotificationContext(
        UUID buyerId,
        UUID sellerId,
        String shipmentId,
        String orderId,
        String trackingCode
) {
}
