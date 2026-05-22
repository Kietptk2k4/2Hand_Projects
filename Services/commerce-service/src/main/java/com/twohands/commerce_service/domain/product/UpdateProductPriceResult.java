package com.twohands.commerce_service.domain.product;

import java.util.UUID;

public record UpdateProductPriceResult(
        UUID productId,
        UUID sellerId,
        UUID shopId,
        ProductStatus status,
        ProductPriceRecord price,
        boolean previousActivePriceClosed
) {
}
