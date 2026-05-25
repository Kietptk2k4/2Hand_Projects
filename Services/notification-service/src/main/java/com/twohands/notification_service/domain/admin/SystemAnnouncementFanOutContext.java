package com.twohands.notification_service.domain.admin;

import java.util.List;
import java.util.UUID;

public record SystemAnnouncementFanOutContext(
        String announcementId,
        String title,
        String content,
        String severity,
        boolean isPinned,
        boolean dismissible,
        List<UUID> explicitRecipientUserIds,
        String targetAudience,
        String referenceType,
        String referenceId
) {
}
