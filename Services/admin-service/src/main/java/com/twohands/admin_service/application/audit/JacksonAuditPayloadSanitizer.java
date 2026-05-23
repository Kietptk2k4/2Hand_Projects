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
	private static final int MAX_CRITICAL_DEPTH = 4;
	private static final int MAX_FIELDS_PER_OBJECT = 40;
	/** FR_LogCriticalAdminActionPayload strict redaction list. */
	private static final Set<String> SENSITIVE_KEYS = Set.of(
			"password",
			"token",
			"otp",
			"secret",
			"authorization",
			"cookie",
			"refresh_token",
			"access_token",
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
			return truncate(redact(node, MAX_CRITICAL_DEPTH + 2).toString());
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
			return truncate(redact(node, MAX_CRITICAL_DEPTH + 2).toString());
		} catch (JsonProcessingException ex) {
			throw serializationFailure(ex);
		}
	}

	@Override
	public String sanitizeCriticalPayload(Map<String, Object> payload) {
		if (payload == null || payload.isEmpty()) {
			throw new AppException(
					ErrorCode.AUDIT_PAYLOAD_ERROR,
					"Critical audit payload must not be empty",
					"payload",
					"must contain audit fields"
			);
		}
		try {
			JsonNode node = objectMapper.valueToTree(payload);
			return truncate(redact(node, MAX_CRITICAL_DEPTH).toString());
		} catch (IllegalArgumentException ex) {
			throw serializationFailure(ex);
		}
	}

	private JsonNode redact(JsonNode node, int maxDepth) {
		return redact(node, maxDepth, 0);
	}

	private JsonNode redact(JsonNode node, int maxDepth, int depth) {
		if (node == null) {
			return objectMapper.nullNode();
		}
		if (depth >= maxDepth) {
			return objectMapper.getNodeFactory().textNode("[MAX_DEPTH]");
		}
		if (node.isObject()) {
			ObjectNode sanitized = objectMapper.createObjectNode();
			int fieldCount = 0;
			Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
			while (fields.hasNext() && fieldCount < MAX_FIELDS_PER_OBJECT) {
				Map.Entry<String, JsonNode> field = fields.next();
				fieldCount++;
				if (isSensitiveKey(field.getKey())) {
					sanitized.put(field.getKey(), "***REDACTED***");
				} else {
					sanitized.set(field.getKey(), redact(field.getValue(), maxDepth, depth + 1));
				}
			}
			if (fields.hasNext()) {
				sanitized.put("_truncated_fields", true);
			}
			return sanitized;
		}
		if (node.isArray()) {
			ArrayNode sanitized = objectMapper.createArrayNode();
			int index = 0;
			for (JsonNode child : node) {
				if (index >= MAX_FIELDS_PER_OBJECT) {
					sanitized.add("[ARRAY_TRUNCATED]");
					break;
				}
				sanitized.add(redact(child, maxDepth, depth + 1));
				index++;
			}
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
		return new AppException(
				ErrorCode.AUDIT_PAYLOAD_ERROR,
				"Failed to serialize critical audit payload: " + ex.getMessage()
		);
	}
}
