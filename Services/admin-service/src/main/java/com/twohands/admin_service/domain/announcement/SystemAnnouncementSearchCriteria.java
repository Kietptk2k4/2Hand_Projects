package com.twohands.admin_service.domain.announcement;

public record SystemAnnouncementSearchCriteria(
		String query,
		SystemAnnouncementStatus status,
		SystemAnnouncementSeverity severity
) {
}
