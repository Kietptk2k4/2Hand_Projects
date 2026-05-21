package com.twohands.commerce_service.delivery.http.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.product.ProductStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductCardResponse(
        @JsonProperty("product_id") UUID productId,
        String title,
        @JsonProperty("thumbnail_url") String thumbnailUrl,
        @JsonProperty("shop_id") UUID shopId,
        @JsonProperty("shop_name") String shopName,
        @JsonProperty("category_id") UUID categoryId,
        String condition,
        ProductStatus status,
        BigDecimal price,
        @JsonProperty("sale_price") BigDecimal salePrice,
        @JsonProperty("effective_price") BigDecimal effectivePrice,
        @JsonProperty("in_stock") boolean inStock,
        @JsonProperty("low_stock") boolean lowStock,
        @JsonProperty("rating_avg") BigDecimal ratingAvg,
        @JsonProperty("rating_count") int ratingCount,
        @JsonProperty("shop_vacation") boolean shopVacation,
        @JsonProperty("vacation_message") String vacationMessage
) {
}
