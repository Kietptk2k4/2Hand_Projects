package com.twohands.commerce_service.domain.shop;

import java.util.UUID;

public record CreateShopDraft(
        UUID sellerId,
        String shopName,
        String description,
        String avatarUrl,
        String coverUrl,
        CreateShopPickupDraft pickupProfile
) {
    public boolean hasPickupProfile() {
        return pickupProfile != null;
    }
}
