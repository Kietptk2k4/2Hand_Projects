package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SellerProductListSummaryResponse(
        long total,
        long active,
        @JsonProperty("out_of_stock") long outOfStock,
        long draft,
        long paused,
        long archived,
        @JsonProperty("low_stock") long lowStock
) {
}
