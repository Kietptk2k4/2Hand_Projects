package com.twohands.admin_service.delivery.http.announcement;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record DismissSystemAnnouncementResponse(
		@JsonProperty("announcement_id")
		UUID announcementId,

		String title,
		String status,
		boolean dismissible,

		@JsonProperty("client_side_persistence")
		boolean clientSidePersistence
) {
}
