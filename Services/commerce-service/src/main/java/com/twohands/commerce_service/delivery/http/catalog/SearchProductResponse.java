package com.twohands.commerce_service.delivery.http.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record SearchProductResponse(
        String keyword,
        List<ProductCardResponse> items,
        PageMetaResponse pagination
) {
}
