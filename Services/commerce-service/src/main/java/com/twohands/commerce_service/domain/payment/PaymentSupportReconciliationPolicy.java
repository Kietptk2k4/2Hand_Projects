package com.twohands.commerce_service.domain.payment;

import java.util.List;

public final class PaymentSupportReconciliationPolicy {

    private PaymentSupportReconciliationPolicy() {
    }

    public static String resolve(
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus,
            List<PaymentWebhookSummary> webhookEvents
    ) {
        if (paymentMethod != PaymentMethod.PAYOS) {
            return "NOT_APPLICABLE";
        }

        boolean hasValidProcessedWebhook = webhookEvents != null && webhookEvents.stream()
                .anyMatch(event -> event.processed() && event.signatureValid());

        return switch (paymentStatus) {
            case PAID -> hasValidProcessedWebhook ? "RECONCILED" : "OUTSTANDING";
            case PENDING -> hasValidProcessedWebhook ? "WEBHOOK_RECEIVED" : "AWAITING_WEBHOOK";
            case FAILED, CANCELLED, EXPIRED -> hasValidProcessedWebhook ? "TERMINAL_RECONCILED" : "TERMINAL_OUTSTANDING";
        };
    }
}
