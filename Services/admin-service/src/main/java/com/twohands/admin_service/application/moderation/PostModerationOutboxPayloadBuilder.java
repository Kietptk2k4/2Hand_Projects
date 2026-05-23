package com.twohands.admin_service.application.moderation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.admin_service.domain.moderation.ContentModerationLog;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class PostModerationOutboxPayloadBuilder {

	private final ObjectMapper objectMapper;

	public PostModerationOutboxPayloadBuilder(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public String buildPostModeratedPayload(ContentModerationLog moderationLog, String postId) {
		Map<String, Object> payload = basePostModerationPayload(moderationLog, postId);
		payload.put("moderated_by", moderationLog.adminId().toString());
		payload.put("moderated_at", moderationLog.createdAt().toString());
		return serialize(payload);
	}

	public String buildPostRestoredPayload(ContentModerationLog moderationLog, String postId) {
		Map<String, Object> payload = basePostModerationPayload(moderationLog, postId);
		payload.put("restored_by", moderationLog.adminId().toString());
		payload.put("restored_at", moderationLog.createdAt().toString());
		return serialize(payload);
	}

	private Map<String, Object> basePostModerationPayload(ContentModerationLog moderationLog, String postId) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("post_id", postId);
		payload.put("moderation_log_id", moderationLog.id().toString());
		payload.put("action", moderationLog.action().name());
		payload.put("reason", moderationLog.reason());
		return payload;
	}

	private String serialize(Map<String, Object> payload) {
		try {
			return objectMapper.writeValueAsString(payload);
		} catch (JsonProcessingException ex) {
			throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to build outbox payload");
		}
	}
}
