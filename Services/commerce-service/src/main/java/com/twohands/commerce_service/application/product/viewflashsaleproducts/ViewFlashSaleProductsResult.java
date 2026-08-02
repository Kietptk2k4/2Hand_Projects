package com.twohands.commerce_service.application.product.viewflashsaleproducts;

import com.twohands.commerce_service.domain.discovery.ProductCardSummary;

import java.time.Instant;
import java.util.List;

public record ViewFlashSaleProductsResult(
        List<ProductCardSummary> items,
        Instant slotStart,
        Instant slotEnd
) {
}
