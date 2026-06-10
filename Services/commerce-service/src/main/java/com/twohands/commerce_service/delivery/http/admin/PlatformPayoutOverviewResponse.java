package com.twohands.commerce_service.delivery.http.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.finance.PlatformPayoutStatusOverview;

import java.math.BigDecimal;
import java.util.List;

public record PlatformPayoutOverviewResponse(
        @JsonProperty("items") List<Item> items
) {
    public record Item(
            @JsonProperty("status") String status,
            @JsonProperty("request_count") long requestCount,
            @JsonProperty("total_amount") BigDecimal totalAmount
    ) {
        public static Item from(PlatformPayoutStatusOverview overview) {
            return new Item(overview.status(), overview.requestCount(), overview.totalAmount());
        }
    }

    public static PlatformPayoutOverviewResponse from(List<PlatformPayoutStatusOverview> overviews) {
        return new PlatformPayoutOverviewResponse(overviews.stream().map(Item::from).toList());
    }
}
