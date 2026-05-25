package com.twohands.notification_service.domain.commerce;

import java.util.UUID;

public record ShipmentDeliveredNotificationContext(
        UUID buyerId,
        String shipmentId,
        String orderId,
        String referenceType,
        String referenceId,
        String deliveredAt
) {
}
