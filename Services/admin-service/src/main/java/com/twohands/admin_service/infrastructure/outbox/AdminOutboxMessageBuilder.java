package com.twohands.admin_service.infrastructure.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.admin_service.domain.outbox.OutboxEvent;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class AdminOutboxMessageBuilder {

	private static final String SOURCE = "admin";

	private final ObjectMapper objectMapper;
	private final AdminOutboxEventKeyResolver eventKeyResolver;

	public AdminOutboxMessageBuilder(ObjectMapper objectMapper, AdminOutboxEventKeyResolver eventKeyResolver) {
		this.objectMapper = objectMapper;
		this.eventKeyResolver = eventKeyResolver;
	}

	public Map<String, Object> buildEnvelope(OutboxEvent event) {
		Map<String, Object> envelope = new LinkedHashMap<>();
		envelope.put("event_id", event.id().toString());
		envelope.put("event_type", event.eventType());
		envelope.put("event_key", eventKeyResolver.resolve(event.eventType(), event.aggregateId()));
		envelope.put("aggregate_id", event.aggregateId().toString());
		envelope.put("source", SOURCE);
		envelope.put("occurred_at", event.createdAt().toString());
		Object payload = parsePayload(event.payload());
		envelope.put("payload", payload);
		applyEnvelopeRouting(envelope, payload);
		return envelope;
	}

	public String buildEnvelopeJson(OutboxEvent event) {
		try {
			return objectMapper.writeValueAsString(buildEnvelope(event));
		} catch (JsonProcessingException ex) {
			throw new AppException(ErrorCode.INTERNAL_ERROR, "Cannot serialize admin outbox envelope: " + ex.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	private void applyEnvelopeRouting(Map<String, Object> envelope, Object payload) {
		if (!(payload instanceof Map<?, ?> payloadMap)) {
			return;
		}
		Map<String, Object> map = (Map<String, Object>) payloadMap;
		List<String> recipientUserIds = new ArrayList<>();

		copyRecipientIds(recipientUserIds, map.get("recipient_user_ids"));
		addRecipient(recipientUserIds, text(map.get("user_id")));
		addRecipient(recipientUserIds, text(map.get("seller_user_id")));
		addRecipient(recipientUserIds, text(map.get("shop_owner_id")));
		addRecipient(recipientUserIds, text(map.get("review_author_id")));

		if (!recipientUserIds.isEmpty()) {
			envelope.put("recipient_user_ids", recipientUserIds);
		}
	}

	private void copyRecipientIds(List<String> recipientUserIds, Object rawRecipients) {
		if (rawRecipients instanceof List<?> values) {
			for (Object value : values) {
				addRecipient(recipientUserIds, text(value));
			}
		}
	}

	private void addRecipient(List<String> recipientUserIds, String candidate) {
		if (candidate == null || candidate.isBlank()) {
			return;
		}
		if (!recipientUserIds.contains(candidate)) {
			recipientUserIds.add(candidate);
		}
	}

	private String text(Object value) {
		if (value == null) {
			return null;
		}
		String text = value.toString().trim();
		return text.isEmpty() ? null : text;
	}

	private Object parsePayload(String payload) {
		if (payload == null || payload.isBlank()) {
			return Map.of();
		}
		try {
			JsonNode node = objectMapper.readTree(payload);
			if (node.isObject() || node.isArray()) {
				return objectMapper.convertValue(node, Object.class);
			}
			return node.asText();
		} catch (JsonProcessingException ex) {
			throw new AppException(ErrorCode.INTERNAL_ERROR, "Invalid admin outbox payload JSON: " + ex.getMessage());
		}
	}
}
