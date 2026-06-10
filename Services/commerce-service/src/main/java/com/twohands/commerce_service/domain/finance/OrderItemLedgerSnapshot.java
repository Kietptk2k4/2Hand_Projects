package com.twohands.commerce_service.domain.finance;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemLedgerSnapshot(
        UUID orderItemId,
        UUID sellerId,
        BigDecimal finalPrice
) {
}
