package com.twohands.admin_service.domain.audit;

import java.util.Map;

public interface AuditPayloadSanitizer {

	String sanitizeToJson(Map<String, Object> payload);

	String sanitizeJson(String rawJson);

	/**
	 * Sanitizes critical audit payload with depth/size limits (FR_LogCriticalAdminActionPayload).
	 * @throws com.twohands.admin_service.exception.AppException when serialization fails
	 */
	String sanitizeCriticalPayload(Map<String, Object> payload);
}
