package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record UpdateProductPriceRequest(
        @NotNull(message = "price is required")
        @DecimalMin(value = "0", message = "price must be greater than or equal to 0")
        BigDecimal price,

        @JsonProperty("sale_price")
        @DecimalMin(value = "0", message = "sale_price must be greater than or equal to 0")
        BigDecimal salePrice,

        @JsonProperty("start_at")
        @NotNull(message = "start_at is required")
        Instant startAt,

        @JsonProperty("end_at")
        Instant endAt
) {
}
