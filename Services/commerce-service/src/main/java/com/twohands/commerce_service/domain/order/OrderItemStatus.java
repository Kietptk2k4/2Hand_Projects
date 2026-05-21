package com.twohands.commerce_service.domain.order;

public enum OrderItemStatus {
    PENDING,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    COMPLETED,
    CANCELLED,
    FAILED,
    RETURNED
}
