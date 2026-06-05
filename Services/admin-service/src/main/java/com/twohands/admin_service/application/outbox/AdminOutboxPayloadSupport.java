package com.twohands.admin_service.application.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class AdminOutboxPayloadSupport {

	private AdminOutboxPayloadSupport() {
	}

	public static void putIfPresent(Map<String, Object> payload, String key, Object value) {
		if (value == null) {
			return;
		}
		if (value instanceof String text && text.isBlank()) {
			return;
		}
		payload.put(key, value);
	}

	public static void putUuid(Map<String, Object> payload, String key, UUID value) {
		if (value != null) {
			payload.put(key, value.toString());
		}
	}

	public static void putRecipientUserIds(Map<String, Object> payload, List<UUID> recipientUserIds) {
		if (recipientUserIds == null || recipientUserIds.isEmpty()) {
			return;
		}
		List<String> serialized = new ArrayList<>();
		for (UUID recipientUserId : recipientUserIds) {
			if (recipientUserId != null) {
				serialized.add(recipientUserId.toString());
			}
		}
		if (!serialized.isEmpty()) {
			payload.put("recipient_user_ids", serialized);
		}
	}

	public static String serialize(ObjectMapper objectMapper, Map<String, Object> payload) {
		Map<String, Object> sanitized = new LinkedHashMap<>();
		for (Map.Entry<String, Object> entry : payload.entrySet()) {
			if (entry.getValue() != null) {
				sanitized.put(entry.getKey(), entry.getValue());
			}
		}
		try {
			return objectMapper.writeValueAsString(sanitized);
		} catch (JsonProcessingException ex) {
			throw new AppException(ErrorCode.INTERNAL_ERROR, "Failed to build outbox payload");
		}
	}
}
