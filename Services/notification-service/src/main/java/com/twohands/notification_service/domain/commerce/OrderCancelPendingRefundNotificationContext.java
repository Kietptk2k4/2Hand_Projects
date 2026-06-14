package com.twohands.notification_service.domain.commerce;

import java.util.UUID;

public record OrderCancelPendingRefundNotificationContext(
        UUID buyerId,
        String orderId,
        String refundRequestId
) {
}
