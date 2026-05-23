package com.twohands.admin_service.domain.audit;

import java.util.Map;

public interface AuditPayloadSanitizer {

	String sanitizeToJson(Map<String, Object> payload);

	String sanitizeJson(String rawJson);
}
