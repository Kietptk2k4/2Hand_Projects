package com.twohands.commerce_service.delivery.http.internal;

import com.twohands.commerce_service.domain.product.ProductStatus;
import com.twohands.commerce_service.domain.product.RestoreProductByAdminResult;

import java.time.Instant;
import java.util.UUID;

public record InternalRestoreProductResponse(
        UUID productId,
        UUID sellerId,
        UUID shopId,
        String title,
        ProductStatus status,
        ProductStatus previousStatus,
        boolean alreadyRestored,
        Instant restoredAt
) {
    public static InternalRestoreProductResponse from(RestoreProductByAdminResult result) {
        return new InternalRestoreProductResponse(
                result.productId(),
                result.sellerId(),
                result.shopId(),
                result.title(),
                result.status(),
                result.previousStatus(),
                result.alreadyRestored(),
                result.restoredAt()
        );
    }
}
