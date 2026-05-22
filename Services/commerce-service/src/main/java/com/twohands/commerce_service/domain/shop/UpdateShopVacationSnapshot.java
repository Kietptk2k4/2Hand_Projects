package com.twohands.commerce_service.domain.shop;

import java.util.UUID;

public record UpdateShopVacationSnapshot(
        UUID shopId,
        UUID sellerId,
        ShopStatus status,
        boolean isVacation,
        String vacationMessage
) {
}
