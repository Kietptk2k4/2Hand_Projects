package com.twohands.commerce_service.domain.shop;

import java.util.UUID;

public record ShopForModeration(
        UUID shopId,
        UUID sellerId,
        String shopName,
        ShopStatus status
) {
}
