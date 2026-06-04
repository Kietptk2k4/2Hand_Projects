package com.twohands.commerce_service.application.payment.processpayoswebhook;

import com.twohands.commerce_service.domain.payment.PaymentFailureOutcome;
import com.twohands.commerce_service.domain.payment.PaymentStatus;
import com.twohands.commerce_service.domain.payment.ProcessPayosPaymentSuccessOutcome;

public record ProcessPayosWebhookResult(
        String eventType,
        String payosOrderCode,
        boolean signatureValid,
        boolean processed,
        PaymentStatus terminalStatus,
        PaymentFailureOutcome failureOutcome,
        ProcessPayosPaymentSuccessOutcome successOutcome,
        boolean successWebhook
) {
    public static ProcessPayosWebhookResult invalidSignature(String eventType, String payosOrderCode) {
        return new ProcessPayosWebhookResult(eventType, payosOrderCode, false, false, null, null, null, false);
    }

    public static ProcessPayosWebhookResult duplicate(String eventType, String payosOrderCode, boolean signatureValid) {
        return new ProcessPayosWebhookResult(eventType, payosOrderCode, signatureValid, true, null, null, null, false);
    }

    public static ProcessPayosWebhookResult processedSuccess(
            String eventType,
            String payosOrderCode,
            ProcessPayosPaymentSuccessOutcome successOutcome
    ) {
        return new ProcessPayosWebhookResult(eventType, payosOrderCode, true, true, null, null, successOutcome, true);
    }

    public static ProcessPayosWebhookResult processedFailure(
            String eventType,
            String payosOrderCode,
            PaymentStatus terminalStatus,
            PaymentFailureOutcome failureOutcome
    ) {
        return new ProcessPayosWebhookResult(
                eventType,
                payosOrderCode,
                true,
                true,
                terminalStatus,
                failureOutcome,
                null,
                false
        );
    }
}
