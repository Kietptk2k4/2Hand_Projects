package com.twohands.commerce_service.domain.order;

public enum OrderStatus {
    CREATED,
    AWAITING_PAYMENT,
    PROCESSING,
    COMPLETED,
    CANCELLED
}
