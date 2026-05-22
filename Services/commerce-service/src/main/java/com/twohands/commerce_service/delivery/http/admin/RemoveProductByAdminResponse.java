package com.twohands.commerce_service.delivery.http.admin;

import com.twohands.commerce_service.domain.product.ProductStatus;

import java.time.Instant;
import java.util.UUID;

public record RemoveProductByAdminResponse(
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
}
