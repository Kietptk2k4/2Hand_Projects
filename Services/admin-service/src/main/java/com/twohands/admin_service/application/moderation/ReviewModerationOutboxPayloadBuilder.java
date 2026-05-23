package com.twohands.admin_service.application.moderation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.admin_service.domain.moderation.ContentModerationLog;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class ReviewModerationOutboxPayloadBuilder {

	private final ObjectMapper objectMapper;

	public ReviewModerationOutboxPayloadBuilder(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public String buildReviewHiddenPayload(ContentModerationLog moderationLog, UUID reviewId) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("review_id", reviewId.toString());
		payload.put("moderation_log_id", moderationLog.id().toString());
		payload.put("action", moderationLog.action().name());
		payload.put("reason", moderationLog.reason());
		payload.put("hidden_by", moderationLog.adminId().toString());
		payload.put("hidden_at", moderationLog.createdAt().toString());
		try {
			return objectMapper.writeValueAsString(payload);
		} catch (JsonProcessingException ex) {
			throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to build outbox payload");
		}
	}
}
