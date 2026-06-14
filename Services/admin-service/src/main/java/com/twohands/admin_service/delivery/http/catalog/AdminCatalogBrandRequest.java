package com.twohands.admin_service.delivery.http.catalog;

import jakarta.validation.constraints.NotBlank;

public record AdminCatalogBrandRequest(
        @NotBlank String name,
        String slug
) {
}
