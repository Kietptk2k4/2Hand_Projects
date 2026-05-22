package com.twohands.commerce_service.domain.order;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProcessSellerOrderItemResult(
        List<ProcessedOrderItemSummary> items,
        int newlyProcessedCount,
        int alreadyProcessingCount,
        Instant processedAt
) {
    public record ProcessedOrderItemSummary(
            UUID orderItemId,
            UUID orderId,
            OrderItemStatus status,
            String productNameSnapshot,
            int quantity,
            boolean newlyProcessed
    ) {
    }
}
