package com.twohands.commerce_service.delivery.http.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.product.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AdminProductListItemResponse(
        @JsonProperty("product_id") UUID productId,
        @JsonProperty("seller_id") UUID sellerId,
        @JsonProperty("shop_id") UUID shopId,
        @JsonProperty("shop_name") String shopName,
        String title,
        @JsonProperty("thumbnail_url") String thumbnailUrl,
        @JsonProperty("category_id") UUID categoryId,
        @JsonProperty("category_name") String categoryName,
        BigDecimal price,
        @JsonProperty("effective_price") BigDecimal effectivePrice,
        ProductStatus status,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("removed_at") Instant removedAt,
        @JsonProperty("remove_reason") String removeReason
) {
}
