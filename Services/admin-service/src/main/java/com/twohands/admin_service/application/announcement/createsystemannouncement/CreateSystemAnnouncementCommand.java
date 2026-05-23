package com.twohands.admin_service.application.announcement.createsystemannouncement;

public record CreateSystemAnnouncementCommand(
		String title,
		String content,
		String severity,
		Boolean pinned,
		Boolean dismissible
) {
}
