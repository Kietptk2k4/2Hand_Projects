package com.twohands.commerce_service.application.shop.updateshopvacation;

import java.util.UUID;

public record UpdateShopVacationCommand(
        UUID sellerId,
        boolean isVacation,
        String vacationMessage
) {
}
