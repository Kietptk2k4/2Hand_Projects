package com.twohands.notification_service.application.read;

public record CleanupOldNotificationsResult(
        int softDeletedCount,
        int batchesProcessed
) {
}
