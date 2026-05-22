package com.twohands.commerce_service.application.shop.updateshopprofile;

import java.util.UUID;

public record UpdateShopProfileCommand(
        UUID sellerId,
        String shopName,
        String description,
        String avatarUrl,
        String coverUrl
) {
}
