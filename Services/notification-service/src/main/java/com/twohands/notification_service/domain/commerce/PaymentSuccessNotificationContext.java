package com.twohands.notification_service.domain.commerce;

import java.util.UUID;

public record PaymentSuccessNotificationContext(
        UUID buyerId,
        String paymentId,
        String orderId,
        String orderCode,
        String referenceType,
        String referenceId,
        String amountSummary
) {
}
