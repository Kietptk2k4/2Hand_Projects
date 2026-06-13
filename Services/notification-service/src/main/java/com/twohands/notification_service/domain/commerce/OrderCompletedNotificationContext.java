package com.twohands.notification_service.domain.commerce;

import java.util.List;
import java.util.UUID;

public record OrderCompletedNotificationContext(
        UUID buyerId,
        String orderId,
        String orderCode,
        String completedAt,
        List<UUID> sellerIds
) {
}
