package com.twohands.commerce_service.delivery.http.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public record ViewProductReviewsResponse(
        @JsonProperty("product_id") UUID productId,
        @JsonProperty("rating_summary") ProductReviewRatingSummaryResponse ratingSummary,
        List<ProductReviewItemResponse> reviews,
        PageMetaResponse pagination
) {
}
