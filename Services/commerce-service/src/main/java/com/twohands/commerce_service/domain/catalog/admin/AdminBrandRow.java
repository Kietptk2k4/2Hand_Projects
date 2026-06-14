package com.twohands.commerce_service.domain.catalog.admin;

import java.time.Instant;
import java.util.UUID;

public record AdminBrandRow(
        UUID id,
        String name,
        String slug,
        boolean active,
        long productCount,
        Instant createdAt,
        Instant updatedAt
) {
}
