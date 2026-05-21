package com.twohands.commerce_service.delivery.http.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public record FilterProductsByCategoryResponse(
        @JsonProperty("category_id") UUID categoryId,
        @JsonProperty("category_name") String categoryName,
        @JsonProperty("category_slug") String categorySlug,
        @JsonProperty("include_children") boolean includeChildren,
        List<ProductCardResponse> items,
        PageMetaResponse pagination
) {
}
