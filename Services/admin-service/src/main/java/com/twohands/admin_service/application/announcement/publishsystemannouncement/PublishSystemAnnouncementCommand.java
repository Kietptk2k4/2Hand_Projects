package com.twohands.admin_service.application.announcement.publishsystemannouncement;

import java.util.List;
import java.util.UUID;

public record PublishSystemAnnouncementCommand(
		UUID announcementId,
		List<UUID> recipientUserIds,
		String targetAudience
) {

	public PublishSystemAnnouncementCommand(UUID announcementId) {
		this(announcementId, List.of(), null);
	}
}
