package com.twohands.commerce_service.delivery.http.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record ViewProductDetailCategoryResponse(
        @JsonProperty("category_id") UUID categoryId,
        String name,
        String slug
) {
}
