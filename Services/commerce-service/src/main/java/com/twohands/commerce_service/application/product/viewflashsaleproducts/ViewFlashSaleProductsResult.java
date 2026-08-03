package com.twohands.commerce_service.application.product.viewflashsaleproducts;

import com.twohands.commerce_service.common.pagination.PageMeta;
import com.twohands.commerce_service.domain.discovery.ProductCardSummary;

import java.time.Instant;
import java.util.List;

public record ViewFlashSaleProductsResult(
        List<ProductCardSummary> items,
        PageMeta pagination,
        Instant slotStart,
        Instant slotEnd
) {
}
