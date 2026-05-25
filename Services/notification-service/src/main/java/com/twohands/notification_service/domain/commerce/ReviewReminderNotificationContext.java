package com.twohands.notification_service.domain.commerce;

import java.util.UUID;

public record ReviewReminderNotificationContext(
        UUID buyerId,
        String orderItemId,
        String orderId,
        String orderCode,
        String productId,
        String productName,
        int reminderDay,
        String referenceType,
        String referenceId,
        boolean alreadyReviewed
) {
}
