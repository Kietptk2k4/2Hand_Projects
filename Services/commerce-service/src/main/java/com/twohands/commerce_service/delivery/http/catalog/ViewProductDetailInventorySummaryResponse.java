package com.twohands.commerce_service.delivery.http.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ViewProductDetailInventorySummaryResponse(
        @JsonProperty("stock_quantity") int stockQuantity,
        @JsonProperty("low_stock_threshold") int lowStockThreshold,
        @JsonProperty("in_stock") boolean inStock,
        @JsonProperty("low_stock") boolean lowStock
) {
}
