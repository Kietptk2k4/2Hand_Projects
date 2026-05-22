package com.twohands.commerce_service.delivery.http.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdateProductReviewRequest(
        @Min(value = 1, message = "rating must be at least 1")
        @Max(value = 5, message = "rating must be at most 5")
        Integer rating,

        String comment
) {
}
