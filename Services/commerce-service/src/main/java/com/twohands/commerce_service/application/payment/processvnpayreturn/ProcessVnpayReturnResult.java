package com.twohands.commerce_service.application.payment.processvnpayreturn;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

public record ProcessVnpayReturnResult(
        URI redirectUri,
        boolean success,
        UUID orderId,
        String txnRef
) {
    public static ProcessVnpayReturnResult success(URI redirectUri, UUID orderId, String txnRef) {
        return new ProcessVnpayReturnResult(redirectUri, true, orderId, txnRef);
    }

    public static ProcessVnpayReturnResult failure(URI redirectUri, UUID orderId, String txnRef) {
        return new ProcessVnpayReturnResult(redirectUri, false, orderId, txnRef);
    }

    public static ProcessVnpayReturnResult unknownError(URI redirectUri) {
        return new ProcessVnpayReturnResult(redirectUri, false, null, null);
    }
}
