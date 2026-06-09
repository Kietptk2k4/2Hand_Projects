package com.twohands.commerce_service.delivery.http.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public record ViewActiveCategoriesResponse(
        List<CategorySummaryResponse> items
) {
    public record CategorySummaryResponse(
            @JsonProperty("category_id") UUID categoryId,
            @JsonProperty("category_name") String categoryName,
            @JsonProperty("category_slug") String categorySlug,
            @JsonProperty("parent_id") UUID parentId,
            int level,
            @JsonProperty("is_leaf") boolean leaf,
            @JsonProperty("product_count") long productCount
    ) {
    }
}
