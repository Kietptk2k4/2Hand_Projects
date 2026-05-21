package com.twohands.commerce_service.domain.discovery;

import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.domain.catalog.ActiveCategory;

import java.util.List;

public record FilterProductsByCategoryResult(
        ActiveCategory category,
        boolean includeChildren,
        List<ProductCardSummary> items,
        PageMeta pagination
) {
}
