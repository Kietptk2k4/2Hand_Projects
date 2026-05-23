package com.twohands.admin_service.unit.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.admin_service.application.audit.JacksonAuditPayloadSanitizer;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JacksonAuditPayloadSanitizerTest {

	private final JacksonAuditPayloadSanitizer sanitizer = new JacksonAuditPayloadSanitizer(new ObjectMapper());

	@Test
	void sanitizeToJson_redactsSensitiveFields() {
		String json = sanitizer.sanitizeToJson(Map.of(
				"reason", "policy violation",
				"password", "secret123",
				"nested", Map.of("access_token", "abc")
		));

		assertTrue(json.contains("policy violation"));
		assertFalse(json.contains("secret123"));
		assertFalse(json.contains("abc"));
		assertTrue(json.contains("***REDACTED***"));
	}

	@Test
	void sanitizeCriticalPayload_includesBeforeAfterAndRedactsSecrets() {
		String json = sanitizer.sanitizeCriticalPayload(Map.of(
				"summary", "Config updated",
				"before", Map.of("value", "10"),
				"after", Map.of("value", "20", "token", "secret-token")
		));

		assertTrue(json.contains("Config updated"));
		assertTrue(json.contains("\"before\""));
		assertTrue(json.contains("***REDACTED***"));
		assertFalse(json.contains("secret-token"));
	}

	@Test
	void sanitizeCriticalPayload_limitsDeepNesting() {
		Map<String, Object> deep = Map.of("l1", Map.of("l2", Map.of("l3", Map.of("l4", Map.of("l5", "deep")))));
		String json = sanitizer.sanitizeCriticalPayload(Map.of("context", deep));

		assertTrue(json.contains("[MAX_DEPTH]") || json.length() < 500);
	}
}
