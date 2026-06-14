package com.twohands.commerce_service.delivery.http.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.domain.catalog.admin.AdminBrandRow;
import com.twohands.commerce_service.domain.catalog.admin.AdminCategoryRow;

import java.time.Instant;
import java.util.UUID;

public final class AdminCatalogMapper {

    private AdminCatalogMapper() {
    }

    public static AdminCategoryResponse toCategoryResponse(AdminCategoryRow row) {
        return new AdminCategoryResponse(
                row.id(),
                row.name(),
                row.slug(),
                row.parentId(),
                row.level(),
                row.path(),
                row.active(),
                row.productCount(),
                row.createdAt(),
                row.updatedAt()
        );
    }

    public static AdminBrandResponse toBrandResponse(AdminBrandRow row) {
        return new AdminBrandResponse(
                row.id(),
                row.name(),
                row.slug(),
                row.active(),
                row.productCount(),
                row.createdAt(),
                row.updatedAt()
        );
    }

    public record AdminCategoryResponse(
            UUID id,
            String name,
            String slug,
            @JsonProperty("parent_id") UUID parentId,
            int level,
            String path,
            @JsonProperty("is_active") boolean active,
            @JsonProperty("product_count") long productCount,
            @JsonProperty("created_at") Instant createdAt,
            @JsonProperty("updated_at") Instant updatedAt
    ) {
    }

    public record AdminBrandResponse(
            UUID id,
            String name,
            String slug,
            @JsonProperty("is_active") boolean active,
            @JsonProperty("product_count") long productCount,
            @JsonProperty("created_at") Instant createdAt,
            @JsonProperty("updated_at") Instant updatedAt
    ) {
    }
}
