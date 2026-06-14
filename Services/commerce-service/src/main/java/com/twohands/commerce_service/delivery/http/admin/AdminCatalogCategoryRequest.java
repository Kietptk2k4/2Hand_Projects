package com.twohands.commerce_service.delivery.http.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record AdminCatalogCategoryRequest(
        @NotBlank String name,
        String slug,
        @JsonProperty("parent_id") UUID parentId
) {
}
