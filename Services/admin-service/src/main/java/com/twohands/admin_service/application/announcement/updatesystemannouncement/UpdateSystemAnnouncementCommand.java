package com.twohands.admin_service.application.announcement.updatesystemannouncement;

import java.util.UUID;

public record UpdateSystemAnnouncementCommand(
		UUID announcementId,
		String title,
		String content,
		String severity,
		Boolean pinned,
		Boolean dismissible
) {
}
