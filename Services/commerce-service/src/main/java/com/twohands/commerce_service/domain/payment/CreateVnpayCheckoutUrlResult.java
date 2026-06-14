package com.twohands.commerce_service.domain.payment;

import java.time.Instant;
import java.util.UUID;

public record CreateVnpayCheckoutUrlResult(
        UUID paymentId,
        UUID orderId,
        String txnRef,
        String checkoutUrl
) {
}
