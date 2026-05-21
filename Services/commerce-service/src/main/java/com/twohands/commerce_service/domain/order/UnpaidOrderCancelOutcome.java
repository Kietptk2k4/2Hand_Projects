package com.twohands.commerce_service.domain.order;

public enum UnpaidOrderCancelOutcome {
    CANCELLED,
    SKIPPED_ALREADY_TERMINAL,
    SKIPPED_PAYMENT_NOT_PENDING,
    SKIPPED_NOT_EXPIRED,
    SKIPPED_COD,
    SKIPPED_SHIPMENT_STARTED,
    SKIPPED_INVENTORY_RELEASE_FAILED
}
