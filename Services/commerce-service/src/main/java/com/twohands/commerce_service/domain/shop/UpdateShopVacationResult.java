package com.twohands.commerce_service.domain.shop;

import java.time.Instant;
import java.util.UUID;

public record UpdateShopVacationResult(
        UUID shopId,
        UUID sellerId,
        ShopStatus status,
        boolean isVacation,
        String vacationMessage,
        Instant updatedAt
) {
}
