package com.twohands.commerce_service.delivery.http.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.finance.PlatformRevenueTrendPoint;

import java.math.BigDecimal;
import java.time.Instant;

public record PlatformRevenueTrendPointResponse(
        @JsonProperty("period_start") Instant periodStart,
        @JsonProperty("gmv_amount") BigDecimal gmvAmount,
        @JsonProperty("platform_fee_amount") BigDecimal platformFeeAmount,
        @JsonProperty("item_count") long itemCount
) {
    public static PlatformRevenueTrendPointResponse from(PlatformRevenueTrendPoint point) {
        return new PlatformRevenueTrendPointResponse(
                point.periodStart(),
                point.gmvAmount(),
                point.platformFeeAmount(),
                point.itemCount()
        );
    }
}
