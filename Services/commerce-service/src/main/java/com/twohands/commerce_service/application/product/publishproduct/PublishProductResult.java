package com.twohands.commerce_service.application.product.publishproduct;

import com.twohands.commerce_service.domain.product.ProductStatus;

import java.time.Instant;
import java.util.UUID;

public record PublishProductResult(
        UUID productId,
        UUID shopId,
        ProductStatus status,
        Instant publishedAt,
        boolean alreadyPublished
) {
}
