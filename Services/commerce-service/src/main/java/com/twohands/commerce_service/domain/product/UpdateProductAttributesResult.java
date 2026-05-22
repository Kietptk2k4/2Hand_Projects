package com.twohands.commerce_service.domain.product;

import java.util.List;
import java.util.UUID;

public record UpdateProductAttributesResult(
        UUID productId,
        UUID sellerId,
        UUID shopId,
        ProductStatus status,
        List<ProductAttributeItem> attributes
) {
}
