package com.twohands.commerce_service.domain.order;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrderItemResult(
        UUID orderItemId,
        UUID productId,
        UUID sellerId,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
