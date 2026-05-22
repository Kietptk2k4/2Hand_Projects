package com.twohands.commerce_service.domain.product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductPriceRecord(
        UUID priceId,
        UUID productId,
        BigDecimal price,
        BigDecimal salePrice,
        BigDecimal effectivePrice,
        Instant startAt,
        Instant endAt,
        Instant createdAt
) {
}
