package com.twohands.commerce_service.delivery.http.internal;

import com.twohands.commerce_service.domain.product.RemoveProductByAdminResult;
import com.twohands.commerce_service.domain.product.ProductStatus;

import java.time.Instant;
import java.util.UUID;

public record InternalRemoveProductResponse(
        UUID productId,
        UUID sellerId,
        UUID shopId,
        String title,
        ProductStatus status,
        ProductStatus previousStatus,
        boolean alreadyRemoved,
        int cartItemsInvalidated,
        Instant removedAt
) {
    public static InternalRemoveProductResponse from(RemoveProductByAdminResult result) {
        return new InternalRemoveProductResponse(
                result.productId(),
                result.sellerId(),
                result.shopId(),
                result.title(),
                result.status(),
                result.previousStatus(),
                result.alreadyRemoved(),
                result.cartItemsInvalidated(),
                result.removedAt()
        );
    }
}
