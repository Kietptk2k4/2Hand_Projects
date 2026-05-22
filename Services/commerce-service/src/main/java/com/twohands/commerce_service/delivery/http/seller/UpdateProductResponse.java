package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.product.ProductStatus;

import java.time.Instant;
import java.util.UUID;

public record UpdateProductResponse(
        @JsonProperty("product_id") UUID productId,
        @JsonProperty("seller_id") UUID sellerId,
        @JsonProperty("shop_id") UUID shopId,
        ProductStatus status,
        @JsonProperty("product_type") String productType,
        @JsonProperty("category_id") UUID categoryId,
        @JsonProperty("brand_id") UUID brandId,
        String condition,
        String title,
        String description,
        @JsonProperty("weight_gram") int weightGram,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt
) {
}
