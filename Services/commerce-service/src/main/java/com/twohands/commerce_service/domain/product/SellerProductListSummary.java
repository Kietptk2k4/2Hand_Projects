package com.twohands.commerce_service.domain.product;

public record SellerProductListSummary(
        long total,
        long active,
        long outOfStock,
        long draft,
        long paused,
        long archived,
        long lowStock
) {
}