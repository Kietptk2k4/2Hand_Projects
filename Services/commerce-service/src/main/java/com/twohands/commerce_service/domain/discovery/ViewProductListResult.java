package com.twohands.commerce_service.domain.discovery;

import com.twohands.commerce_service.common.pagination.PageMeta;

import java.util.List;

public record ViewProductListResult(
        List<ProductCardSummary> items,
        PageMeta pagination
) {
}
