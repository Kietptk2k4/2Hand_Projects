package com.twohands.commerce_service.application.product.updateproductprice;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record UpdateProductPriceCommand(
        UUID sellerId,
        UUID productId,
        BigDecimal price,
        BigDecimal salePrice,
        Instant startAt,
        Instant endAt
) {
}
