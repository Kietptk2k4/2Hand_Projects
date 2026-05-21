package com.twohands.commerce_service.application.shop.createshop;

import com.twohands.commerce_service.domain.shop.CreateShopPickupDraft;

import java.util.UUID;

public record CreateShopCommand(
        UUID sellerId,
        String shopName,
        String description,
        String avatarUrl,
        String coverUrl,
        CreateShopPickupDraft pickupProfile
) {
}
