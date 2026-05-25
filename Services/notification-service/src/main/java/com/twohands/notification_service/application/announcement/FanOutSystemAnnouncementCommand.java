package com.twohands.notification_service.application.announcement;

import com.twohands.notification_service.domain.admin.SystemAnnouncementFanOutContext;

import java.util.List;
import java.util.UUID;

public record FanOutSystemAnnouncementCommand(
        UUID notificationEventId,
        SystemAnnouncementFanOutContext context,
        List<UUID> recipientUserIds
) {
}
