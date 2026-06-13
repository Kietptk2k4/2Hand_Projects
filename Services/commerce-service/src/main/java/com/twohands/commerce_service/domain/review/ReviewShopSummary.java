package com.twohands.commerce_service.domain.review;

import java.util.UUID;

public record ReviewShopSummary(
        UUID shopId,
        String shopName,
        String avatarUrl,
        UUID sellerId
) {
}
