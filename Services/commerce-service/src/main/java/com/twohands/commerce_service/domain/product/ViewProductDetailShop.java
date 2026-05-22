package com.twohands.commerce_service.domain.product;

import java.util.UUID;

public record ViewProductDetailShop(
        UUID shopId,
        String shopName,
        String avatarUrl,
        String coverUrl
) {
}
