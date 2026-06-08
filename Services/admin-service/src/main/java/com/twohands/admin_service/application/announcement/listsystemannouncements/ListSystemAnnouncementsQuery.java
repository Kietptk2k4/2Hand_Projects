package com.twohands.admin_service.application.announcement.listsystemannouncements;

public record ListSystemAnnouncementsQuery(
		String query,
		String status,
		String severity,
		Integer page,
		Integer size
) {
}
