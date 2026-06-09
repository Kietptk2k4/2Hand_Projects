package com.twohands.commerce_service.domain.catalog;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryReadRepository {

    Optional<ActiveCategory> findById(UUID categoryId);

    List<UUID> resolveCategoryIdsForFilter(UUID categoryId, String categoryPath, boolean includeChildren);

    List<CategorySummary> findActiveSummaries(
            Integer minLevel,
            Integer maxLevel,
            boolean leafOnly,
            boolean includeProductCounts,
            Instant now
    );
}
