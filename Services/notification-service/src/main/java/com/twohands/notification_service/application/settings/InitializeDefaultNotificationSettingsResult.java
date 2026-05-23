package com.twohands.notification_service.application.settings;

import java.util.UUID;

public record InitializeDefaultNotificationSettingsResult(
        UUID userId,
        int createdCount,
        int skippedCount
) {
}
