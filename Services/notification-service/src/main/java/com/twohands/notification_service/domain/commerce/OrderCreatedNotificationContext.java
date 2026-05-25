package com.twohands.notification_service.domain.commerce;

import java.util.List;
import java.util.UUID;

public record OrderCreatedNotificationContext(
        UUID buyerId,
        String orderId,
        String orderCode,
        List<UUID> sellerIds,
        String totalAmountSummary
) {
}
