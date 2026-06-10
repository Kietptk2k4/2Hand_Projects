package com.twohands.commerce_service.domain.finance;

import java.math.BigDecimal;
import java.util.UUID;

public record PlatformTopSeller(
        UUID sellerId,
        String shopName,
        BigDecimal recognizedGross,
        BigDecimal platformFee,
        long completedItemCount
) {
}
