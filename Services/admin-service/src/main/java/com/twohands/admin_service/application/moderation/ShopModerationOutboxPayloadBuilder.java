package com.twohands.admin_service.application.moderation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.admin_service.application.outbox.AdminOutboxPayloadSupport;
import com.twohands.admin_service.domain.moderation.ContentModerationLog;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class ShopModerationOutboxPayloadBuilder {

	private final ObjectMapper objectMapper;

	public ShopModerationOutboxPayloadBuilder(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public String buildShopSuspendedPayload(
			ContentModerationLog moderationLog,
			UUID shopId,
			UUID shopOwnerId
	) {
		Map<String, Object> payload = baseShopModerationPayload(moderationLog, shopId);
		AdminOutboxPayloadSupport.putUuid(payload, "shop_owner_id", shopOwnerId);
		AdminOutboxPayloadSupport.putIfPresent(payload, "suspension_reason", moderationLog.reason());
		payload.put("suspended_by", moderationLog.adminId().toString());
		payload.put("suspended_at", moderationLog.createdAt().toString());
		return AdminOutboxPayloadSupport.serialize(objectMapper, payload);
	}

	public String buildShopClosedPayload(ContentModerationLog moderationLog, UUID shopId, UUID shopOwnerId) {
		Map<String, Object> payload = baseShopModerationPayload(moderationLog, shopId);
		AdminOutboxPayloadSupport.putUuid(payload, "shop_owner_id", shopOwnerId);
		payload.put("closed_by", moderationLog.adminId().toString());
		payload.put("closed_at", moderationLog.createdAt().toString());
		return AdminOutboxPayloadSupport.serialize(objectMapper, payload);
	}

	public String buildShopRestoredPayload(ContentModerationLog moderationLog, UUID shopId, UUID shopOwnerId) {
		Map<String, Object> payload = baseShopModerationPayload(moderationLog, shopId);
		AdminOutboxPayloadSupport.putUuid(payload, "shop_owner_id", shopOwnerId);
		payload.put("restored_by", moderationLog.adminId().toString());
		payload.put("restored_at", moderationLog.createdAt().toString());
		return AdminOutboxPayloadSupport.serialize(objectMapper, payload);
	}

	private Map<String, Object> baseShopModerationPayload(ContentModerationLog moderationLog, UUID shopId) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("shop_id", shopId.toString());
		payload.put("moderation_log_id", moderationLog.id().toString());
		payload.put("action", moderationLog.action().name());
		AdminOutboxPayloadSupport.putIfPresent(payload, "reason", moderationLog.reason());
		return payload;
	}
}
