package com.twohands.commerce_service.application.shop.moderateshop;

import com.twohands.commerce_service.domain.shop.ShopModerationAction;

import java.util.UUID;

public record ModerateShopCommand(
        UUID adminId,
        UUID shopId,
        ShopModerationAction action,
        String reason
) {
}
