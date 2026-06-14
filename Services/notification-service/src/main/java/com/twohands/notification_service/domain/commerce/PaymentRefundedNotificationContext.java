package com.twohands.notification_service.domain.commerce;

import java.util.UUID;

public record PaymentRefundedNotificationContext(
        UUID buyerId,
        String paymentId,
        String orderId,
        String refundRequestId
) {
}
