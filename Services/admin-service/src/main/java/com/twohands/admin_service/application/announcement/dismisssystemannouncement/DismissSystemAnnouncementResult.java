package com.twohands.admin_service.application.announcement.dismisssystemannouncement;

import com.twohands.admin_service.domain.announcement.SystemAnnouncementStatus;

import java.util.UUID;

public record DismissSystemAnnouncementResult(
		UUID announcementId,
		String title,
		SystemAnnouncementStatus status,
		boolean dismissible,
		boolean clientSidePersistence
) {
}
