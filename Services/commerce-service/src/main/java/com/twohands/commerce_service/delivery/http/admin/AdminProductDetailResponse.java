package com.twohands.commerce_service.delivery.http.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.product.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AdminProductDetailResponse(
        @JsonProperty("product_id") UUID productId,
        @JsonProperty("seller_id") UUID sellerId,
        @JsonProperty("shop_id") UUID shopId,
        @JsonProperty("shop_name") String shopName,
        String title,
        String description,
        ProductStatus status,
        @JsonProperty("category_id") UUID categoryId,
        @JsonProperty("category_name") String categoryName,
        BigDecimal price,
        @JsonProperty("effective_price") BigDecimal effectivePrice,
        @JsonProperty("stock_quantity") Integer stockQuantity,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt,
        @JsonProperty("removed_at") Instant removedAt,
        @JsonProperty("remove_reason") String removeReason,
        @JsonProperty("open_order_count") long openOrderCount,
        List<AdminProductDetailMediaResponse> media,
        List<AdminProductDetailAttributeResponse> attributes
) {
}
