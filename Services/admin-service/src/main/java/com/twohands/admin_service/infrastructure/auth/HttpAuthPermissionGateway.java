package com.twohands.admin_service.infrastructure.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.twohands.admin_service.domain.auth.AuthPermissionGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "admin.integrations.auth.enabled", havingValue = "true")
public class HttpAuthPermissionGateway implements AuthPermissionGateway {

	private static final Logger log = LoggerFactory.getLogger(HttpAuthPermissionGateway.class);

	private final RestClient restClient;
	private volatile boolean available = true;

	public HttpAuthPermissionGateway(@Value("${admin.integrations.auth.base-url}") String baseUrl) {
		this.restClient = RestClient.builder()
				.baseUrl(trimTrailingSlash(baseUrl))
				.build();
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isAvailable() {
		return available;
	}

	@Override
	public boolean hasPermission(UUID adminId, String permissionCode, String bearerToken) {
		try {
			JsonNode root = restClient.get()
					.uri("/api/v1/admin/users/{userId}/permissions", adminId)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
					.accept(MediaType.APPLICATION_JSON)
					.retrieve()
					.body(JsonNode.class);
			available = true;
			if (root == null || !root.path("success").asBoolean(false)) {
				return false;
			}
			JsonNode permissions = root.path("data").path("permissions");
			if (!permissions.isArray()) {
				return false;
			}
			for (JsonNode permission : permissions) {
				if (permissionCode.equals(permission.path("code").asText())) {
					return true;
				}
			}
			return false;
		} catch (RestClientException ex) {
			available = false;
			log.warn("Auth permission lookup failed for adminId={}: {}", adminId, ex.getMessage());
			return false;
		}
	}

	private String trimTrailingSlash(String baseUrl) {
		if (baseUrl == null || baseUrl.isBlank()) {
			return "http://localhost:3001";
		}
		return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
	}
}
