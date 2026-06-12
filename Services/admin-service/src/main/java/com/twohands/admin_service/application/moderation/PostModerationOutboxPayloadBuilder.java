package com.twohands.admin_service.application.moderation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.admin_service.application.outbox.AdminOutboxPayloadSupport;
import com.twohands.admin_service.domain.moderation.ContentModerationLog;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class PostModerationOutboxPayloadBuilder {

	private final ObjectMapper objectMapper;

	public PostModerationOutboxPayloadBuilder(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public String buildPostModeratedPayload(
			ContentModerationLog moderationLog,
			String postId,
			UUID authorUserId
	) {
		Map<String, Object> payload = basePostModerationPayload(moderationLog, postId);
		AdminOutboxPayloadSupport.putUuid(payload, "author_user_id", authorUserId);
		payload.put("moderated_by", moderationLog.adminId().toString());
		payload.put("moderated_at", moderationLog.createdAt().toString());
		return AdminOutboxPayloadSupport.serialize(objectMapper, payload);
	}

	public String buildPostRestoredPayload(ContentModerationLog moderationLog, String postId) {
		Map<String, Object> payload = basePostModerationPayload(moderationLog, postId);
		payload.put("restored_by", moderationLog.adminId().toString());
		payload.put("restored_at", moderationLog.createdAt().toString());
		return AdminOutboxPayloadSupport.serialize(objectMapper, payload);
	}

	private Map<String, Object> basePostModerationPayload(ContentModerationLog moderationLog, String postId) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("post_id", postId);
		payload.put("moderation_log_id", moderationLog.id().toString());
		payload.put("action", moderationLog.action().name());
		AdminOutboxPayloadSupport.putIfPresent(payload, "reason", moderationLog.reason());
		return payload;
	}
}
