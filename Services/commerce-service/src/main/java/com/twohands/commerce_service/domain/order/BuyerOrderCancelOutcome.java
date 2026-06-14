package com.twohands.commerce_service.domain.order;

public enum BuyerOrderCancelOutcome {
    CANCELLED,
    PENDING_REFUND,
    REFUND_ALREADY_REQUESTED,
    ALREADY_CANCELLED,
    NOT_CANCELLABLE,
    NOT_FOUND
}
