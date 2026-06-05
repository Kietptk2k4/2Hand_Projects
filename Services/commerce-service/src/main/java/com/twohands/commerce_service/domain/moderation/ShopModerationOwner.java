package com.twohands.commerce_service.domain.moderation;

import java.util.UUID;

public record ShopModerationOwner(
        UUID shopId,
        UUID sellerId
) {
}
