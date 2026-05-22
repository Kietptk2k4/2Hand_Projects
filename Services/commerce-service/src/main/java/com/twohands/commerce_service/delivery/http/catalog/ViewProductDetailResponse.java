package com.twohands.commerce_service.delivery.http.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.product.ProductStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ViewProductDetailResponse(
        @JsonProperty("product_id") UUID productId,
        String title,
        String description,
        String condition,
        @JsonProperty("weight_gram") int weightGram,
        ProductStatus status,
        ViewProductDetailCategoryResponse category,
        ViewProductDetailShopResponse shop,
        List<ViewProductDetailMediaResponse> media,
        List<ViewProductDetailAttributeResponse> attributes,
        BigDecimal price,
        @JsonProperty("sale_price") BigDecimal salePrice,
        @JsonProperty("effective_price") BigDecimal effectivePrice,
        @JsonProperty("inventory_summary") ViewProductDetailInventorySummaryResponse inventorySummary,
        @JsonProperty("rating_avg") BigDecimal ratingAvg,
        @JsonProperty("rating_count") int ratingCount,
        @JsonProperty("shop_vacation") boolean shopVacation,
        @JsonProperty("vacation_message") String vacationMessage
) {
}
