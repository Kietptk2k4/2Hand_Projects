package com.twohands.admin_service.application.announcement.pinsystemannouncement;

import com.twohands.admin_service.domain.announcement.SystemAnnouncementStatus;

import java.util.UUID;

public record PinSystemAnnouncementResult(
		UUID announcementId,
		String title,
		SystemAnnouncementStatus status,
		boolean pinned,
		boolean stateChanged
) {
}
