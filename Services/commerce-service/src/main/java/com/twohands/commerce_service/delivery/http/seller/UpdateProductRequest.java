package com.twohands.commerce_service.delivery.http.seller;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateProductRequest(
        @JsonProperty("product_type")
        @NotBlank(message = "product_type is required")
        String productType,

        @JsonProperty("category_id")
        @NotNull(message = "category_id is required")
        UUID categoryId,

        @JsonProperty("brand_id")
        UUID brandId,

        @NotBlank(message = "condition is required")
        String condition,

        @NotBlank(message = "title is required")
        @Size(max = 500, message = "title must be at most 500 characters")
        String title,

        @NotBlank(message = "description is required")
        String description,

        @JsonProperty("weight_gram")
        @NotNull(message = "weight_gram is required")
        @Positive(message = "weight_gram must be greater than 0")
        Integer weightGram
) {
}
