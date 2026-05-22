package com.twohands.commerce_service.domain.order;

import java.time.Instant;

public record OrderStatusHistoryEntry(
        OrderStatus oldStatus,
        OrderStatus newStatus,
        String changedBy,
        String note,
        Instant occurredAt
) {
}
