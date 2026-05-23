package com.twohands.admin_service.delivery.http.announcement;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record CancelSystemAnnouncementResponse(
		@JsonProperty("announcement_id")
		UUID announcementId,

		String title,
		String status,

		@JsonProperty("state_changed")
		boolean stateChanged,

		@JsonProperty("outbox_event_id")
		UUID outboxEventId
) {
}
