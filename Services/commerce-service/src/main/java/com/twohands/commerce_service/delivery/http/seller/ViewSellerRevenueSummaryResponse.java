package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.finance.SellerRevenueSummary;

import java.math.BigDecimal;
import java.time.Instant;

public record ViewSellerRevenueSummaryResponse(
        @JsonProperty("in_transit") SellerRevenueBucketResponse inTransit,
        @JsonProperty("pending_confirm") SellerRevenueBucketResponse pendingConfirm,
        @JsonProperty("recognized") SellerRevenueBucketResponse recognized,
        @JsonProperty("total_gross") BigDecimal totalGross,
        @JsonProperty("balance") SellerBalanceSummaryResponse balance,
        @JsonProperty("from") Instant from,
        @JsonProperty("to") Instant to
) {
    public static ViewSellerRevenueSummaryResponse from(SellerRevenueSummary summary) {
        return new ViewSellerRevenueSummaryResponse(
                SellerRevenueBucketResponse.from(summary.inTransit()),
                SellerRevenueBucketResponse.from(summary.pendingConfirm()),
                SellerRevenueBucketResponse.from(summary.recognized()),
                summary.totalGross(),
                SellerBalanceSummaryResponse.from(summary.balance()),
                summary.from(),
                summary.to()
        );
    }
}
