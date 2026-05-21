package com.twohands.commerce_service.application.product.archiveproduct;

import com.twohands.commerce_service.domain.product.ProductStatus;

import java.time.Instant;
import java.util.UUID;

public record ArchiveProductResult(
        UUID productId,
        UUID shopId,
        ProductStatus status,
        Instant archivedAt,
        int cartItemsInvalidated,
        boolean alreadyArchived
) {
}
