package com.twohands.commerce_service.domain.payment;

import java.time.Instant;

public record PayosPaymentLinkResult(
        String payosOrderCode,
        String payosCheckoutUrl,
        Instant checkoutUrlExpiredAt,
        String providerResponseJson,
        boolean mockProvider
) {
}
