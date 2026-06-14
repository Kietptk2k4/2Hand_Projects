package com.twohands.commerce_service.domain.payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record VnpayCreatePaymentUrlCommand(
        UUID paymentId,
        UUID orderId,
        BigDecimal amount,
        String txnRef,
        String orderDescription,
        String clientIp,
        Instant occurredAt
) {
}
