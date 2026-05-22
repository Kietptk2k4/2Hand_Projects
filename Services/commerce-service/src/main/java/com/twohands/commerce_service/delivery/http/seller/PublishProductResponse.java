package com.twohands.commerce_service.delivery.http.seller;

import com.twohands.commerce_service.domain.product.ProductStatus;

import java.time.Instant;
import java.util.UUID;

public record PublishProductResponse(
        UUID productId,
        UUID shopId,
        ProductStatus status,
        Instant publishedAt,
        boolean alreadyPublished
) {
}
