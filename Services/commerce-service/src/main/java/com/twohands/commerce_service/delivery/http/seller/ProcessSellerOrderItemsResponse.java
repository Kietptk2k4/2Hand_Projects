package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.order.OrderItemStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProcessSellerOrderItemsResponse(
        List<ProcessedOrderItemResponse> items,
        @JsonProperty("newly_processed_count") int newlyProcessedCount,
        @JsonProperty("already_processing_count") int alreadyProcessingCount,
        @JsonProperty("processed_at") Instant processedAt
) {
    public record ProcessedOrderItemResponse(
            @JsonProperty("order_item_id") UUID orderItemId,
            @JsonProperty("order_id") UUID orderId,
            OrderItemStatus status,
            @JsonProperty("product_name_snapshot") String productNameSnapshot,
            int quantity,
            @JsonProperty("newly_processed") boolean newlyProcessed
    ) {
    }
}
