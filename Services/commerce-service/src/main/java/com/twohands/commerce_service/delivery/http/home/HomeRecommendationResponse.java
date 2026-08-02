package com.twohands.commerce_service.delivery.http.home;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record HomeRecommendationResponse(
        @JsonProperty("request_id") String requestId,
        @JsonProperty("ranking_mode") String rankingMode,
        @JsonProperty("model_name") String modelName,
        @JsonProperty("model_version") Integer modelVersion,
        List<HomeRecommendationItemResponse> items
) {
    public record HomeRecommendationItemResponse(
            UUID id,
            String title,
            BigDecimal price,
            String thumbnail,
            ShopSummaryResponse shop,
            RatingSummaryResponse rating
    ) {
    }

    public record ShopSummaryResponse(UUID id, String name) {
    }

    public record RatingSummaryResponse(BigDecimal avg, int count) {
    }
}
