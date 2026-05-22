package com.twohands.commerce_service.domain.payment;

public enum PaymentFailureOutcome {
    PROCESSED,
    SKIPPED_ALREADY_TERMINAL,
    SKIPPED_PAYMENT_NOT_PENDING,
    SKIPPED_ALREADY_PAID,
    SKIPPED_COD,
    SKIPPED_SHIPMENT_STARTED,
    SKIPPED_ORDER_NOT_CANCELLABLE
}
