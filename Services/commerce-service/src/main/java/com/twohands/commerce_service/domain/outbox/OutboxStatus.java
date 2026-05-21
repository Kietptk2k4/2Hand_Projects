package com.twohands.commerce_service.domain.outbox;

public enum OutboxStatus {
    PENDING,
    PROCESSING,
    PUBLISHED,
    FAILED
}
