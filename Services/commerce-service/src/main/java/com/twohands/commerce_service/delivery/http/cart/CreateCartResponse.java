package com.twohands.commerce_service.delivery.http.cart;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CreateCartResponse(
        @JsonProperty("cart_id") UUID cartId,
        @JsonProperty("user_id") UUID userId,
        @JsonProperty("items") List<CreateCartItemResponse> items,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt
) {
}
