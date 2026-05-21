package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.product.ProductStatus;

import java.time.Instant;
import java.util.UUID;

public record CreateProductResponse(
        @JsonProperty("product_id") UUID productId,
        @JsonProperty("seller_id") UUID sellerId,
        @JsonProperty("shop_id") UUID shopId,
        @JsonProperty("status") ProductStatus status,
        @JsonProperty("product_type") String productType,
        @JsonProperty("category_id") UUID categoryId,
        @JsonProperty("brand_id") UUID brandId,
        @JsonProperty("condition") String condition,
        @JsonProperty("title") String title,
        @JsonProperty("description") String description,
        @JsonProperty("weight_gram") int weightGram,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt
) {
}
