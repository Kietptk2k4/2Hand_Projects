package com.twohands.admin_service.infrastructure.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;

import java.util.UUID;

final class CommerceIntegrationJsonSupport {

	private CommerceIntegrationJsonSupport() {
	}

	static void requireSuccess(JsonNode root) {
		if (root == null || !root.path("success").asBoolean(false)) {
			throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage());
		}
	}

	static UUID requireUuid(JsonNode data, String field) {
		UUID parsed = parseUuid(data, field);
		if (parsed == null) {
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service returned an invalid response");
		}
		return parsed;
	}

	static UUID parseUuid(JsonNode data, String field) {
		if (data == null || data.isMissingNode() || data.isNull()) {
			return null;
		}
		JsonNode node = data.get(field);
		if (node == null || node.isNull()) {
			return null;
		}
		String raw = node.asText(null);
		if (raw == null || raw.isBlank()) {
			return null;
		}
		try {
			return UUID.fromString(raw.trim());
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}

	static String trimTrailingSlash(String baseUrl) {
		if (baseUrl == null || baseUrl.isBlank()) {
			return "http://localhost:3003";
		}
		return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
	}
}
