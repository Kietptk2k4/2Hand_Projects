package com.twohands.admin_service.application.moderation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.admin_service.application.outbox.AdminOutboxPayloadSupport;
import com.twohands.admin_service.domain.moderation.ContentModerationLog;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class CommentModerationOutboxPayloadBuilder {

	private final ObjectMapper objectMapper;

	public CommentModerationOutboxPayloadBuilder(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public String buildCommentModeratedPayload(
			ContentModerationLog moderationLog,
			String commentId,
			UUID authorUserId,
			String postId
	) {
		Map<String, Object> payload = baseCommentModerationPayload(moderationLog, commentId);
		AdminOutboxPayloadSupport.putUuid(payload, "author_user_id", authorUserId);
		AdminOutboxPayloadSupport.putIfPresent(payload, "post_id", postId);
		payload.put("moderated_by", moderationLog.adminId().toString());
		payload.put("moderated_at", moderationLog.createdAt().toString());
		return AdminOutboxPayloadSupport.serialize(objectMapper, payload);
	}

	public String buildCommentRestoredPayload(
			ContentModerationLog moderationLog,
			String commentId,
			UUID authorUserId,
			String postId
	) {
		Map<String, Object> payload = baseCommentModerationPayload(moderationLog, commentId);
		AdminOutboxPayloadSupport.putUuid(payload, "author_user_id", authorUserId);
		AdminOutboxPayloadSupport.putIfPresent(payload, "post_id", postId);
		payload.put("restored_by", moderationLog.adminId().toString());
		payload.put("restored_at", moderationLog.createdAt().toString());
		return AdminOutboxPayloadSupport.serialize(objectMapper, payload);
	}

	private Map<String, Object> baseCommentModerationPayload(ContentModerationLog moderationLog, String commentId) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("comment_id", commentId);
		payload.put("moderation_log_id", moderationLog.id().toString());
		payload.put("action", moderationLog.action().name());
		AdminOutboxPayloadSupport.putIfPresent(payload, "reason", moderationLog.reason());
		return payload;
	}
}
