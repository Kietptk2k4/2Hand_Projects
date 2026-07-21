package com.twohands.social_service.domain.integration;

import java.util.Set;
import java.util.UUID;

public record UserProductAffinity(
        Set<String> categoryIds,
        Set<String> shopIds
) {
    public static UserProductAffinity empty() {
        return new UserProductAffinity(Set.of(), Set.of());
    }

    public UserProductAffinity {
        categoryIds = categoryIds == null ? Set.of() : Set.copyOf(categoryIds);
        shopIds = shopIds == null ? Set.of() : Set.copyOf(shopIds);
    }

    public static UserProductAffinity of(UUID ignoredUserId, Set<String> categoryIds, Set<String> shopIds) {
        return new UserProductAffinity(categoryIds, shopIds);
    }
}
