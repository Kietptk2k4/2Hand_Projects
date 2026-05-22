package com.twohands.commerce_service.domain.product;

import java.util.UUID;

public record UpdateProductAttributesProductRef(
        UUID productId,
        UUID sellerId,
        UUID shopId,
        ProductStatus status
) {
}
