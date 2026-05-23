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
public class CommentModerationOutboxPayloadBuilder {

	private final ObjectMapper objectMapper;

	public CommentModerationOutboxPayloadBuilder(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public String buildCommentModeratedPayload(ContentModerationLog moderationLog, String commentId) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("comment_id", commentId);
		payload.put("moderation_log_id", moderationLog.id().toString());
		payload.put("action", moderationLog.action().name());
		payload.put("reason", moderationLog.reason());
		payload.put("moderated_by", moderationLog.adminId().toString());
		payload.put("moderated_at", moderationLog.createdAt().toString());
		return serialize(payload);
	}

	private String serialize(Map<String, Object> payload) {
		try {
			return objectMapper.writeValueAsString(payload);
		} catch (JsonProcessingException ex) {
			throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to build outbox payload");
		}
	}
}
