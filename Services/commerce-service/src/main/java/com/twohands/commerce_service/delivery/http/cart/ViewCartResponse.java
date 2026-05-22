package com.twohands.commerce_service.delivery.http.cart;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ViewCartResponse(
        @JsonProperty("cart_id") UUID cartId,
        List<ViewCartItemResponse> items,
        ViewCartSummaryResponse summary,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt
) {
}
