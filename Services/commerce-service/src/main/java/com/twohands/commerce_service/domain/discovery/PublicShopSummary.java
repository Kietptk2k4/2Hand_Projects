package com.twohands.commerce_service.domain.discovery;

import java.math.BigDecimal;
import java.util.UUID;

public record PublicShopSummary(
        UUID shopId,
        String shopName,
        String description,
        String avatarUrl,
        String coverUrl,
        BigDecimal ratingAvg,
        int ratingCount,
        boolean shopVacation,
        String vacationMessage
) {
}
