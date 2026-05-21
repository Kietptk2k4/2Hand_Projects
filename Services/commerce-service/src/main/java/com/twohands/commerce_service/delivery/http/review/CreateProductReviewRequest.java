package com.twohands.commerce_service.delivery.http.review;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateProductReviewRequest(
        @JsonProperty("order_item_id")
        @NotNull(message = "order_item_id is required")
        UUID orderItemId,

        @NotNull(message = "rating is required")
        @Min(value = 1, message = "rating must be at least 1")
        @Max(value = 5, message = "rating must be at most 5")
        Integer rating,

        String comment
) {
}
