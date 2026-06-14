package com.twohands.notification_service.domain.commerce;

import java.util.UUID;

public record OrderCancelledNotificationContext(
        UUID buyerId,
        String orderId
) {
}
