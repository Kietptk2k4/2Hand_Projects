package com.twohands.admin_service.application.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.twohands.admin_service.domain.audit.AuditPayloadSanitizer;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@Component
public class JacksonAuditPayloadSanitizer implements AuditPayloadSanitizer {

	private static final int MAX_JSON_LENGTH = 8_000;
	private static final Set<String> SENSITIVE_KEYS = Set.of(
			"password",
			"token",
			"authorization",
			"secret",
			"otp",
			"refresh_token",
			"access_token",
			"cookie",
			"api_key",
			"credential",
			"bearer"
	);

	private final ObjectMapper objectMapper;

	public JacksonAuditPayloadSanitizer(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public String sanitizeToJson(Map<String, Object> payload) {
		if (payload == null || payload.isEmpty()) {
			return null;
		}
		try {
			JsonNode node = objectMapper.valueToTree(payload);
			return truncate(redact(node).toString());
		} catch (IllegalArgumentException ex) {
			throw serializationFailure(ex);
		}
	}

	@Override
	public String sanitizeJson(String rawJson) {
		if (rawJson == null || rawJson.isBlank()) {
			return null;
		}
		try {
			JsonNode node = objectMapper.readTree(rawJson);
			return truncate(redact(node).toString());
		} catch (JsonProcessingException ex) {
			throw serializationFailure(ex);
		}
	}

	private JsonNode redact(JsonNode node) {
		if (node == null) {
			return objectMapper.nullNode();
		}
		if (node.isObject()) {
			ObjectNode sanitized = objectMapper.createObjectNode();
			Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
			while (fields.hasNext()) {
				Map.Entry<String, JsonNode> field = fields.next();
				if (isSensitiveKey(field.getKey())) {
					sanitized.put(field.getKey(), "***REDACTED***");
				} else {
					sanitized.set(field.getKey(), redact(field.getValue()));
				}
			}
			return sanitized;
		}
		if (node.isArray()) {
			ArrayNode sanitized = objectMapper.createArrayNode();
			node.forEach(child -> sanitized.add(redact(child)));
			return sanitized;
		}
		return node;
	}

	private boolean isSensitiveKey(String key) {
		if (key == null) {
			return false;
		}
		String normalized = key.toLowerCase().replace('-', '_');
		return SENSITIVE_KEYS.stream().anyMatch(normalized::contains);
	}

	private String truncate(String json) {
		if (json.length() <= MAX_JSON_LENGTH) {
			return json;
		}
		return json.substring(0, MAX_JSON_LENGTH) + "...[TRUNCATED]";
	}

	private AppException serializationFailure(Exception ex) {
		return new AppException(ErrorCode.BAD_REQUEST, "Failed to serialize audit payload: " + ex.getMessage());
	}
}
