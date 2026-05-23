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
public class ShopModerationOutboxPayloadBuilder {

	private final ObjectMapper objectMapper;

	public ShopModerationOutboxPayloadBuilder(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public String buildShopSuspendedPayload(ContentModerationLog moderationLog, UUID shopId) {
		Map<String, Object> payload = baseShopModerationPayload(moderationLog, shopId);
		payload.put("suspended_by", moderationLog.adminId().toString());
		payload.put("suspended_at", moderationLog.createdAt().toString());
		return serialize(payload);
	}

	public String buildShopClosedPayload(ContentModerationLog moderationLog, UUID shopId) {
		Map<String, Object> payload = baseShopModerationPayload(moderationLog, shopId);
		payload.put("closed_by", moderationLog.adminId().toString());
		payload.put("closed_at", moderationLog.createdAt().toString());
		return serialize(payload);
	}

	private Map<String, Object> baseShopModerationPayload(ContentModerationLog moderationLog, UUID shopId) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("shop_id", shopId.toString());
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
