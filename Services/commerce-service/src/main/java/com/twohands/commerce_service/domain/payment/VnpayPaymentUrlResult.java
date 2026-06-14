package com.twohands.commerce_service.domain.payment;

public record VnpayPaymentUrlResult(
        String txnRef,
        String paymentUrl,
        String providerResponseJson,
        boolean mock
) {
}
