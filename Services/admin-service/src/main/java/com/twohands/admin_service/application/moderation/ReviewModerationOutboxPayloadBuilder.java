package com.twohands.admin_service.application.moderation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.admin_service.application.outbox.AdminOutboxPayloadSupport;
import com.twohands.admin_service.domain.moderation.ContentModerationLog;
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

	public String buildReviewHiddenPayload(
			ContentModerationLog moderationLog,
			UUID reviewId,
			UUID reviewAuthorId,
			UUID sellerUserId
	) {
		Map<String, Object> payload = baseReviewModerationPayload(moderationLog, reviewId);
		AdminOutboxPayloadSupport.putUuid(payload, "review_author_id", reviewAuthorId);
		AdminOutboxPayloadSupport.putUuid(payload, "seller_user_id", sellerUserId);
		AdminOutboxPayloadSupport.putIfPresent(payload, "hidden_reason", moderationLog.reason());
		payload.put("hidden_by", moderationLog.adminId().toString());
		payload.put("hidden_at", moderationLog.createdAt().toString());
		return AdminOutboxPayloadSupport.serialize(objectMapper, payload);
	}

	public String buildReviewRemovedPayload(
			ContentModerationLog moderationLog,
			UUID reviewId,
			UUID reviewAuthorId,
			UUID sellerUserId
	) {
		Map<String, Object> payload = baseReviewModerationPayload(moderationLog, reviewId);
		AdminOutboxPayloadSupport.putUuid(payload, "review_author_id", reviewAuthorId);
		AdminOutboxPayloadSupport.putUuid(payload, "seller_user_id", sellerUserId);
		payload.put("removed_by", moderationLog.adminId().toString());
		payload.put("removed_at", moderationLog.createdAt().toString());
		return AdminOutboxPayloadSupport.serialize(objectMapper, payload);
	}

	public String buildReviewRestoredPayload(
			ContentModerationLog moderationLog,
			UUID reviewId,
			UUID reviewAuthorId,
			UUID sellerUserId
	) {
		Map<String, Object> payload = baseReviewModerationPayload(moderationLog, reviewId);
		AdminOutboxPayloadSupport.putUuid(payload, "review_author_id", reviewAuthorId);
		AdminOutboxPayloadSupport.putUuid(payload, "seller_user_id", sellerUserId);
		payload.put("restored_by", moderationLog.adminId().toString());
		payload.put("restored_at", moderationLog.createdAt().toString());
		return AdminOutboxPayloadSupport.serialize(objectMapper, payload);
	}

	private Map<String, Object> baseReviewModerationPayload(ContentModerationLog moderationLog, UUID reviewId) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("review_id", reviewId.toString());
		payload.put("moderation_log_id", moderationLog.id().toString());
		payload.put("action", moderationLog.action().name());
		AdminOutboxPayloadSupport.putIfPresent(payload, "reason", moderationLog.reason());
		return payload;
	}
}
