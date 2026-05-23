package com.twohands.notification_service.domain.idempotency;

public interface NotificationErrorSanitizer {

    String sanitize(String errorMessage);
}
