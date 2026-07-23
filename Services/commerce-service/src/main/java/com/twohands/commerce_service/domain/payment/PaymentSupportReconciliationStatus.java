package com.twohands.commerce_service.domain.payment;

public enum PaymentSupportReconciliationStatus {
    NOT_APPLICABLE,
    RECONCILED,
    OUTSTANDING,
    AWAITING_WEBHOOK,
    WEBHOOK_RECEIVED,
    TERMINAL_RECONCILED,
    TERMINAL_OUTSTANDING
}
