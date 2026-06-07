package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record SellerShipmentListSummaryResponse(
        @JsonProperty("status_counts") Map<String, Long> statusCounts
) {
}
