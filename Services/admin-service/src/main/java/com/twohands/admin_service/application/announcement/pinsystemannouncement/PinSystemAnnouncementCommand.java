package com.twohands.admin_service.application.announcement.pinsystemannouncement;

import java.util.UUID;

public record PinSystemAnnouncementCommand(
		UUID announcementId,
		boolean pinned
) {
}
