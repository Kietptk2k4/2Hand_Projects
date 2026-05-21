package com.twohands.commerce_service.domain.payment;

import java.time.Instant;
import java.util.UUID;

public record CreatePayosCheckoutUrlResult(
        UUID paymentId,
        UUID orderId,
        String payosOrderCode,
        String payosCheckoutUrl,
        Instant checkoutUrlExpiredAt,
        boolean reusedExistingUrl
) {
}
