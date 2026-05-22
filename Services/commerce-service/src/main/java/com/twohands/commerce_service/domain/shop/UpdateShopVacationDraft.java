package com.twohands.commerce_service.domain.shop;

import java.util.UUID;

public record UpdateShopVacationDraft(
        UUID shopId,
        boolean isVacation,
        String vacationMessage
) {
}
