package com.twohands.admin_service.domain.announcement;

import java.time.Instant;
import java.util.UUID;

public record SystemAnnouncement(
		UUID id,
		String title,
		String content,
		SystemAnnouncementSeverity severity,
		boolean pinned,
		boolean dismissible,
		SystemAnnouncementStatus status,
		UUID createdBy,
		Instant createdAt,
		Instant sentAt
) {
}
