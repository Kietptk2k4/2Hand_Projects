package com.twohands.commerce_service.domain.payment;

import java.time.Instant;
import java.util.UUID;

public record PayosCreateLinkCommand(
        UUID paymentId,
        UUID orderId,
        long orderCode,
        long amountVnd,
        String description,
        Instant linkExpiredAt
) {
}
