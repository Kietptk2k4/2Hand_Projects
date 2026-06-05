package com.twohands.admin_service.unit.announcement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.admin_service.application.announcement.SystemAnnouncementOutboxPayloadBuilder;
import com.twohands.admin_service.domain.announcement.SystemAnnouncement;
import com.twohands.admin_service.domain.announcement.SystemAnnouncementSeverity;
import com.twohands.admin_service.domain.announcement.SystemAnnouncementStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SystemAnnouncementOutboxPayloadBuilderTest {

	private final SystemAnnouncementOutboxPayloadBuilder builder =
			new SystemAnnouncementOutboxPayloadBuilder(new ObjectMapper());

	@Test
	void buildPublishedPayload_includesRecipientUserIds() throws Exception {
		UUID announcementId = UUID.randomUUID();
		UUID recipientOne = UUID.randomUUID();
		UUID recipientTwo = UUID.randomUUID();
		SystemAnnouncement announcement = announcement(announcementId);

		String json = builder.buildPublishedPayload(
				announcement,
				List.of(recipientOne, recipientTwo),
				null
		);
		JsonNode payload = new ObjectMapper().readTree(json);

		assertThat(payload.get("announcement_id").asText()).isEqualTo(announcementId.toString());
		assertThat(payload.get("title").asText()).isEqualTo("Platform maintenance");
		assertThat(payload.get("content").asText()).isEqualTo("Scheduled downtime tonight");
		assertThat(payload.get("severity").asText()).isEqualTo("WARNING");
		assertThat(payload.get("recipient_user_ids")).hasSize(2);
		assertThat(payload.get("recipient_user_ids").get(0).asText()).isEqualTo(recipientOne.toString());
		assertThat(payload.has("target_audience")).isFalse();
	}

	@Test
	void buildPublishedPayload_includesTargetAudience() throws Exception {
		SystemAnnouncement announcement = announcement(UUID.randomUUID());

		String json = builder.buildPublishedPayload(announcement, List.of(), "ALL_ACTIVE_USERS");
		JsonNode payload = new ObjectMapper().readTree(json);

		assertThat(payload.get("target_audience").asText()).isEqualTo("ALL_ACTIVE_USERS");
		assertThat(payload.has("recipient_user_ids")).isFalse();
	}

	private SystemAnnouncement announcement(UUID announcementId) {
		return new SystemAnnouncement(
				announcementId,
				"Platform maintenance",
				"Scheduled downtime tonight",
				SystemAnnouncementSeverity.WARNING,
				true,
				true,
				SystemAnnouncementStatus.SENT,
				UUID.randomUUID(),
				Instant.parse("2026-06-04T09:00:00Z"),
				Instant.parse("2026-06-04T10:00:00Z")
		);
	}
}
