package com.twohands.admin_service.application.announcement.listsystemannouncements;

import java.util.List;

public record ListSystemAnnouncementsResult(
		int page,
		int size,
		long totalElements,
		int totalPages,
		List<SystemAnnouncementListItem> items
) {
}
