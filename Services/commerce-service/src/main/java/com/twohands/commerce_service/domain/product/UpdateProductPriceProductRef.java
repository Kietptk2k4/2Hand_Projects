package com.twohands.commerce_service.domain.product;

import java.util.UUID;

public record UpdateProductPriceProductRef(
        UUID productId,
        UUID sellerId,
        UUID shopId,
        ProductStatus status
) {
}
