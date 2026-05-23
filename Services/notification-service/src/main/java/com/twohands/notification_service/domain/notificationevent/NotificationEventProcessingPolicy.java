package com.twohands.notification_service.domain.notificationevent;

public final class NotificationEventProcessingPolicy {

    private NotificationEventProcessingPolicy() {
    }

    public static boolean isTerminal(NotificationEventStatus status) {
        return status == NotificationEventStatus.COMPLETED;
    }

    public static boolean isRetryable(NotificationEvent event) {
        return event.status() == NotificationEventStatus.FAILED
                && event.retryCount() < event.maxRetryCount();
    }

    public static boolean canProcess(NotificationEvent event) {
        return event.status() == NotificationEventStatus.PENDING || isRetryable(event);
    }

    public static boolean shouldSkipDuplicateProcessing(NotificationEvent event) {
        return isTerminal(event.status());
    }

    public static boolean isPermanentFailure(NotificationEvent event) {
        return event.status() == NotificationEventStatus.FAILED
                && event.retryCount() >= event.maxRetryCount();
    }
}
