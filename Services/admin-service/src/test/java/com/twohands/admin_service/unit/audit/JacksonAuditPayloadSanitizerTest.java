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
}
