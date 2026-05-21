package com.twohands.commerce_service.domain.catalog;

import java.util.UUID;

public record ActiveCategory(
        UUID id,
        String name,
        String slug,
        String path
) {
    public String subtreePathPrefix() {
        if (path == null || path.isBlank()) {
            return id.toString() + "%";
        }
        return path.endsWith("/") ? path + "%" : path + "/%";
    }
}
