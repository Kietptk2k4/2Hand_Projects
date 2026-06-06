package com.twohands.commerce_service.domain.product;

import java.util.UUID;

public record UpdateProductMediaProductRef(
        UUID productId,
        UUID sellerId,
        UUID shopId,
        ProductStatus status
) {
}
