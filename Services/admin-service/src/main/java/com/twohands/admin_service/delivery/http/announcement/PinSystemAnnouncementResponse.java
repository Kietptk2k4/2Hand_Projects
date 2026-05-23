package com.twohands.admin_service.delivery.http.announcement;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record PinSystemAnnouncementResponse(
		@JsonProperty("announcement_id")
		UUID announcementId,

		String title,
		String status,

		@JsonProperty("is_pinned")
		boolean pinned,

		@JsonProperty("state_changed")
		boolean stateChanged
) {
}
