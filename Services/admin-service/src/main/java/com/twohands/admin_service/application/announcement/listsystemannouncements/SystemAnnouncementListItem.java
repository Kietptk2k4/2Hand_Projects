package com.twohands.admin_service.application.announcement.listsystemannouncements;

import com.twohands.admin_service.domain.announcement.SystemAnnouncementSeverity;
import com.twohands.admin_service.domain.announcement.SystemAnnouncementStatus;

import java.time.Instant;
import java.util.UUID;

public record SystemAnnouncementListItem(
		UUID announcementId,
		String title,
		String content,
		SystemAnnouncementSeverity severity,
		SystemAnnouncementStatus status,
		boolean pinned,
		boolean dismissible,
		UUID createdBy,
		Instant createdAt,
		Instant sentAt
) {
}
