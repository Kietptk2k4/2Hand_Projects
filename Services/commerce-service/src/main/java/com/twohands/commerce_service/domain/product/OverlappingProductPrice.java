package com.twohands.commerce_service.domain.product;

import java.time.Instant;
import java.util.UUID;

public record OverlappingProductPrice(
        UUID priceId,
        Instant startAt,
        Instant endAt
) {
}
