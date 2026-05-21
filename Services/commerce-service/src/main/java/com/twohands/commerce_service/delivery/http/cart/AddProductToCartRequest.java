package com.twohands.commerce_service.delivery.http.cart;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddProductToCartRequest(
        @JsonProperty("product_id")
        @NotNull(message = "product_id is required")
        UUID productId,
        @Min(value = 1, message = "quantity must be greater than 0")
        int quantity
) {
}
