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
public class ProductModerationOutboxPayloadBuilder {

	private final ObjectMapper objectMapper;

	public ProductModerationOutboxPayloadBuilder(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public String buildProductRemovedPayload(ContentModerationLog moderationLog, UUID productId) {
		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("product_id", productId.toString());
		payload.put("moderation_log_id", moderationLog.id().toString());
		payload.put("action", moderationLog.action().name());
		payload.put("reason", moderationLog.reason());
		payload.put("removed_by", moderationLog.adminId().toString());
		payload.put("removed_at", moderationLog.createdAt().toString());
		try {
			return objectMapper.writeValueAsString(payload);
		} catch (JsonProcessingException ex) {
			throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to build outbox payload");
		}
	}
}
