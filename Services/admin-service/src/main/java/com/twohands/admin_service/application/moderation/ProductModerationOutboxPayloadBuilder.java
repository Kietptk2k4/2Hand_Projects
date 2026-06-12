package com.twohands.admin_service.application.moderation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.admin_service.application.outbox.AdminOutboxPayloadSupport;
import com.twohands.admin_service.domain.moderation.ContentModerationLog;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class ProductModerationOutboxPayloadBuilder {

	private final ObjectMapper objectMapper;

	public ProductModerationOutboxPayloadBuilder(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public String buildProductRemovedPayload(
			ContentModerationLog moderationLog,
			UUID productId,
			UUID sellerUserId
	) {
		Map<String, Object> payload = baseProductModerationPayload(moderationLog, productId);
		AdminOutboxPayloadSupport.putUuid(payload, "seller_user_id", sellerUserId);
		AdminOutboxPayloadSupport.putIfPresent(payload, "removal_reason", moderationLog.reason());
		payload.put("removed_by", moderationLog.adminId().toString());
		payload.put("removed_at", moderationLog.createdAt().toString());
		return AdminOutboxPayloadSupport.serialize(objectMapper, payload);
	}

	public String buildProductRestoredPayload(
			ContentModerationLog moderationLog,
			UUID productId,
			UUID sellerUserId
	) {
		Map<String, Object> payload = baseProductModerationPayload(moderationLog, productId);
		AdminOutboxPayloadSupport.putUuid(payload, "seller_user_id", sellerUserId);
		payload.put("restored_by", moderationLog.adminId().toString());
		payload.put("restored_at", moderationLog.createdAt().toString());
		return AdminOutboxPayloadSupport.serialize(objectMapper, payload);
	}

	private Map<String, Object> baseProductModerationPayload(ContentModerationLog moderationLog, UUID productId) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("product_id", productId.toString());
		payload.put("moderation_log_id", moderationLog.id().toString());
		payload.put("action", moderationLog.action().name());
		AdminOutboxPayloadSupport.putIfPresent(payload, "reason", moderationLog.reason());
		return payload;
	}
}
