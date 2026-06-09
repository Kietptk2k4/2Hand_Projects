package com.twohands.commerce_service.domain.catalog;

import java.util.UUID;

public record CategorySummary(
        UUID id,
        String name,
        String slug,
        UUID parentId,
        int level,
        boolean leaf,
        long productCount
) {
}
