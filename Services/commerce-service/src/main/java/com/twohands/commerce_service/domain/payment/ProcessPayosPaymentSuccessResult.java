package com.twohands.commerce_service.domain.payment;

import java.time.Instant;
import java.util.UUID;

public record ProcessPayosPaymentSuccessResult(
        ProcessPayosPaymentSuccessOutcome outcome,
        UUID paymentId,
        UUID orderId,
        Instant processedAt
) {
    public static ProcessPayosPaymentSuccessResult notFound() {
        return new ProcessPayosPaymentSuccessResult(ProcessPayosPaymentSuccessOutcome.NOT_FOUND, null, null, null);
    }

    public static ProcessPayosPaymentSuccessResult skippedAlreadyPaid(UUID paymentId, UUID orderId) {
        return new ProcessPayosPaymentSuccessResult(
                ProcessPayosPaymentSuccessOutcome.SKIPPED_ALREADY_PAID,
                paymentId,
                orderId,
                null
        );
    }

    public static ProcessPayosPaymentSuccessResult skippedNotPending(UUID paymentId, UUID orderId) {
        return new ProcessPayosPaymentSuccessResult(
                ProcessPayosPaymentSuccessOutcome.SKIPPED_NOT_PENDING,
                paymentId,
                orderId,
                null
        );
    }

    public static ProcessPayosPaymentSuccessResult processed(UUID paymentId, UUID orderId, Instant processedAt) {
        return new ProcessPayosPaymentSuccessResult(
                ProcessPayosPaymentSuccessOutcome.PROCESSED,
                paymentId,
                orderId,
                processedAt
        );
    }
}
