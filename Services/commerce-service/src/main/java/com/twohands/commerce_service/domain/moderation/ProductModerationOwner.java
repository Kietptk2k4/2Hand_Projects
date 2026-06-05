package com.twohands.commerce_service.domain.moderation;

import java.util.UUID;

public record ProductModerationOwner(
        UUID productId,
        UUID sellerId,
        UUID shopId
) {
}
