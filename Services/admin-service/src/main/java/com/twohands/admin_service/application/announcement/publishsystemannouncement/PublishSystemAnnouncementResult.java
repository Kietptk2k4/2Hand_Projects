package com.twohands.admin_service.application.announcement.publishsystemannouncement;

import com.twohands.admin_service.domain.announcement.SystemAnnouncementSeverity;
import com.twohands.admin_service.domain.announcement.SystemAnnouncementStatus;

import java.time.Instant;
import java.util.UUID;

public record PublishSystemAnnouncementResult(
		UUID announcementId,
		String title,
		SystemAnnouncementSeverity severity,
		SystemAnnouncementStatus status,
		boolean pinned,
		boolean dismissible,
		Instant sentAt,
		UUID outboxEventId
) {
}
