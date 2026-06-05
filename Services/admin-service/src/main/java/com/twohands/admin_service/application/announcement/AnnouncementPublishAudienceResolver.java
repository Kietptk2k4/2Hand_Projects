package com.twohands.admin_service.application.announcement;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class AnnouncementPublishAudienceResolver {

	private final List<UUID> configuredDevRecipientUserIds;

	public AnnouncementPublishAudienceResolver(
			@Value("${admin.announcements.dev-recipient-user-ids:}") String devRecipientUserIds
	) {
		this.configuredDevRecipientUserIds = parseRecipientUserIds(devRecipientUserIds);
	}

	public List<UUID> resolveRecipients(List<UUID> requestedRecipientUserIds) {
		if (requestedRecipientUserIds != null && !requestedRecipientUserIds.isEmpty()) {
			return List.copyOf(requestedRecipientUserIds);
		}
		return configuredDevRecipientUserIds;
	}

	private List<UUID> parseRecipientUserIds(String raw) {
		if (raw == null || raw.isBlank()) {
			return List.of();
		}
		List<UUID> recipients = new ArrayList<>();
		for (String token : raw.split(",")) {
			if (token == null || token.isBlank()) {
				continue;
			}
			recipients.add(UUID.fromString(token.trim()));
		}
		return List.copyOf(recipients);
	}
}
