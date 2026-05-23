package com.twohands.admin_service.application.announcement.cancelsystemannouncement;

import com.twohands.admin_service.domain.announcement.SystemAnnouncementStatus;

import java.util.UUID;

public record CancelSystemAnnouncementResult(
		UUID announcementId,
		String title,
		SystemAnnouncementStatus status,
		boolean stateChanged,
		UUID outboxEventId
) {
}
