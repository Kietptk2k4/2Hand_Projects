package com.twohands.commerce_service.domain.finance;

import java.math.BigDecimal;

public record PlatformPayoutStatusOverview(
        String status,
        long requestCount,
        BigDecimal totalAmount
) {
}
