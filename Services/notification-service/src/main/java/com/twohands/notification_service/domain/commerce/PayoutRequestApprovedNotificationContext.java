package com.twohands.notification_service.domain.commerce;

import java.math.BigDecimal;
import java.util.UUID;

public record PayoutRequestApprovedNotificationContext(
        UUID sellerId,
        String payoutRequestId,
        BigDecimal amount,
        String approvedAt
) {
}
