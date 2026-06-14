package com.twohands.notification_service.domain.commerce;

import java.util.List;
import java.util.UUID;

public record OrderCancelledNotificationContext(
        UUID buyerId,
        String orderId,
        List<UUID> sellerIds,
        String reason,
        String cancelledBy,
        UUID cancelledByUserId,
        String refundRequestedBy
) {
}
