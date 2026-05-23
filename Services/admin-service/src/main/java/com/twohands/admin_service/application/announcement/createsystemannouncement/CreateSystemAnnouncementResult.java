package com.twohands.admin_service.application.announcement.createsystemannouncement;

import com.twohands.admin_service.domain.announcement.SystemAnnouncementSeverity;
import com.twohands.admin_service.domain.announcement.SystemAnnouncementStatus;

import java.time.Instant;
import java.util.UUID;

public record CreateSystemAnnouncementResult(
		UUID announcementId,
		String title,
		String content,
		SystemAnnouncementSeverity severity,
		boolean pinned,
		boolean dismissible,
		SystemAnnouncementStatus status,
		UUID createdBy,
		Instant createdAt
) {
}
