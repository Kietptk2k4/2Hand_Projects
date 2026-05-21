package com.twohands.commerce_service.application.cart.createcart;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CreateCartResult(
        UUID cartId,
        UUID userId,
        List<CreateCartItemResult> items,
        Instant createdAt,
        Instant updatedAt,
        boolean newlyCreated
) {
}
