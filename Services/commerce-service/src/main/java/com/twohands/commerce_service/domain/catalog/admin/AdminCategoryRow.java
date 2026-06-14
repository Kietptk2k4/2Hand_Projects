package com.twohands.commerce_service.domain.catalog.admin;

import java.time.Instant;
import java.util.UUID;

public record AdminCategoryRow(
        UUID id,
        String name,
        String slug,
        UUID parentId,
        int level,
        String path,
        boolean active,
        long productCount,
        Instant createdAt,
        Instant updatedAt
) {
}
