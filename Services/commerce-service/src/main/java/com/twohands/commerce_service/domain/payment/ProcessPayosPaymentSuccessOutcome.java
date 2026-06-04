package com.twohands.commerce_service.domain.payment;

public enum ProcessPayosPaymentSuccessOutcome {
    PROCESSED,
    SKIPPED_ALREADY_PAID,
    SKIPPED_NOT_PENDING,
    NOT_FOUND
}
