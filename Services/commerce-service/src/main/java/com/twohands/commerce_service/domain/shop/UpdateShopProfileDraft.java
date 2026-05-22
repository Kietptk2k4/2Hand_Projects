package com.twohands.commerce_service.domain.shop;

import java.util.UUID;

public record UpdateShopProfileDraft(
        UUID shopId,
        UUID sellerId,
        String shopName,
        String description,
        String avatarUrl,
        String coverUrl
) {
}
