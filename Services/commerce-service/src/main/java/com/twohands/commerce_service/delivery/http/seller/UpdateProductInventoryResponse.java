package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.product.ProductStatus;

import java.time.Instant;
import java.util.UUID;

public record UpdateProductInventoryResponse(
        @JsonProperty("product_id") UUID productId,
        @JsonProperty("seller_id") UUID sellerId,
        @JsonProperty("shop_id") UUID shopId,
        ProductStatus status,
        @JsonProperty("previous_status") ProductStatus previousStatus,
        @JsonProperty("status_changed") boolean statusChanged,
        @JsonProperty("stock_quantity") int stockQuantity,
        @JsonProperty("low_stock_threshold") int lowStockThreshold,
        @JsonProperty("reserved_quantity") int reservedQuantity,
        @JsonProperty("cart_items_synced") int cartItemsSynced,
        @JsonProperty("updated_at") Instant updatedAt
) {
}
