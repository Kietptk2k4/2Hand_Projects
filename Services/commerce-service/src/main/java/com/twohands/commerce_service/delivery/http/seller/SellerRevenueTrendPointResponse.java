package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.finance.SellerRevenueTrendPoint;

import java.math.BigDecimal;
import java.time.Instant;

public record SellerRevenueTrendPointResponse(
        @JsonProperty("period_start") Instant periodStart,
        @JsonProperty("recognized_amount") BigDecimal recognizedAmount,
        @JsonProperty("item_count") long itemCount
) {
    public static SellerRevenueTrendPointResponse from(SellerRevenueTrendPoint point) {
        return new SellerRevenueTrendPointResponse(
                point.periodStart(),
                point.recognizedAmount(),
                point.itemCount()
        );
    }
}
