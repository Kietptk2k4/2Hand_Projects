package com.twohands.admin_service.infrastructure.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.admin_service.domain.outbox.OutboxEvent;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
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
		envelope.put("payload", parsePayload(event.payload()));
		return envelope;
	}

	public String buildEnvelopeJson(OutboxEvent event) {
		try {
			return objectMapper.writeValueAsString(buildEnvelope(event));
		} catch (JsonProcessingException ex) {
			throw new AppException(ErrorCode.INTERNAL_ERROR, "Cannot serialize admin outbox envelope: " + ex.getMessage());
		}
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
