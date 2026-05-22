package com.twohands.commerce_service.domain.product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record UpdateProductPriceDraft(
        UUID productId,
        BigDecimal price,
        BigDecimal salePrice,
        Instant startAt,
        Instant endAt
) {
}
