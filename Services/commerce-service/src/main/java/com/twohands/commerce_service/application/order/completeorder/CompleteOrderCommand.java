package com.twohands.commerce_service.application.order.completeorder;

import java.util.UUID;

public record CompleteOrderCommand(
        UUID orderId,
        String reason,
        String changedBy,
        String completedByOutbox
) {
}
