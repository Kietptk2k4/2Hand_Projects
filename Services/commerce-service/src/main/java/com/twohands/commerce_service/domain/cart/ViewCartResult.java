package com.twohands.commerce_service.domain.cart;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ViewCartResult(
        UUID cartId,
        List<ViewCartItem> items,
        ViewCartSummary summary,
        Instant createdAt,
        Instant updatedAt
) {
}
