package com.twohands.admin_service.application.announcement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.admin_service.domain.announcement.SystemAnnouncement;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class SystemAnnouncementOutboxPayloadBuilder {

	private final ObjectMapper objectMapper;

	public SystemAnnouncementOutboxPayloadBuilder(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public String buildPublishedPayload(SystemAnnouncement announcement) {
		return toJson(buildCommonPayload(announcement));
	}

	public String buildCancelledPayload(
			SystemAnnouncement announcement,
			String previousStatus,
			Instant cancelledAt
	) {
		Map<String, Object> payload = buildCommonPayload(announcement);
		payload.put("previous_status", previousStatus);
		payload.put("cancelled_at", cancelledAt.toString());
		return toJson(payload);
	}

	private Map<String, Object> buildCommonPayload(SystemAnnouncement announcement) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("announcement_id", announcement.id().toString());
		payload.put("title", announcement.title());
		payload.put("content", announcement.content());
		payload.put("severity", announcement.severity().name());
		payload.put("is_pinned", announcement.pinned());
		payload.put("dismissible", announcement.dismissible());
		payload.put("status", announcement.status().name());
		payload.put("sent_at", announcement.sentAt() != null ? announcement.sentAt().toString() : null);
		payload.put("created_by", announcement.createdBy().toString());
		return payload;
	}

	private String toJson(Map<String, Object> payload) {
		try {
			return objectMapper.writeValueAsString(payload);
		} catch (JsonProcessingException ex) {
			throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to build outbox payload");
		}
	}
}
