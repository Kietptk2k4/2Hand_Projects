package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateProductInventoryRequest(
        @JsonProperty("stock_quantity")
        @NotNull(message = "stock_quantity is required")
        @Min(value = 0, message = "stock_quantity must be greater than or equal to 0")
        Integer stockQuantity,

        @JsonProperty("low_stock_threshold")
        @Min(value = 0, message = "low_stock_threshold must be greater than or equal to 0")
        Integer lowStockThreshold
) {
}
