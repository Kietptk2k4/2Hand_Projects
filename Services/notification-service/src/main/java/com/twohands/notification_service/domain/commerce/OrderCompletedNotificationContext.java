package com.twohands.notification_service.domain.commerce;

import java.util.UUID;

public record OrderCompletedNotificationContext(
        UUID buyerId,
        String orderId,
        String orderCode,
        String completedAt
) {
}
