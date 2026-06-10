package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.finance.SellerRevenueTrendResult;

import java.time.Instant;
import java.util.List;

public record ViewSellerRevenueTrendResponse(
        @JsonProperty("granularity") String granularity,
        @JsonProperty("from") Instant from,
        @JsonProperty("to") Instant to,
        @JsonProperty("points") List<SellerRevenueTrendPointResponse> points
) {
    public static ViewSellerRevenueTrendResponse from(SellerRevenueTrendResult result) {
        return new ViewSellerRevenueTrendResponse(
                result.granularity().name(),
                result.from(),
                result.to(),
                result.points().stream().map(SellerRevenueTrendPointResponse::from).toList()
        );
    }
}
