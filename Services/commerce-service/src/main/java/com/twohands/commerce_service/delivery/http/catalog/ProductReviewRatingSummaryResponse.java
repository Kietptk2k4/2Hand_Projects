package com.twohands.commerce_service.delivery.http.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record ProductReviewRatingSummaryResponse(
        @JsonProperty("rating_avg") BigDecimal ratingAvg,
        @JsonProperty("rating_count") int ratingCount
) {
}
