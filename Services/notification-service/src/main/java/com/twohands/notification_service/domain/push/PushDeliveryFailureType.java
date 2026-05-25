package com.twohands.notification_service.domain.push;

public enum PushDeliveryFailureType {
    RETRYABLE,
    PERMANENT,
    INVALID_TOKEN
}
