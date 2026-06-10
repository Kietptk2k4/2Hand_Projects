package com.twohands.commerce_service.delivery.http.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.finance.PlatformRevenueTrendResult;
import com.twohands.commerce_service.domain.finance.RevenueTrendGranularity;

import java.time.Instant;
import java.util.List;

public record ViewPlatformRevenueTrendResponse(
        @JsonProperty("granularity") RevenueTrendGranularity granularity,
        @JsonProperty("from") Instant from,
        @JsonProperty("to") Instant to,
        @JsonProperty("points") List<PlatformRevenueTrendPointResponse> points
) {
    public static ViewPlatformRevenueTrendResponse from(PlatformRevenueTrendResult result) {
        return new ViewPlatformRevenueTrendResponse(
                result.granularity(),
                result.from(),
                result.to(),
                result.points().stream().map(PlatformRevenueTrendPointResponse::from).toList()
        );
    }
}
