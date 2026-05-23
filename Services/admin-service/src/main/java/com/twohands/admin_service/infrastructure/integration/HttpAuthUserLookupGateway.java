package com.twohands.admin_service.infrastructure.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.twohands.admin_service.domain.integration.AuthUserLookupGateway;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "admin.integrations.auth.enabled", havingValue = "true")
public class HttpAuthUserLookupGateway implements AuthUserLookupGateway {

	private static final Logger log = LoggerFactory.getLogger(HttpAuthUserLookupGateway.class);

	private final RestClient restClient;

	public HttpAuthUserLookupGateway(@Value("${admin.integrations.auth.base-url}") String baseUrl) {
		this.restClient = RestClient.builder()
				.baseUrl(trimTrailingSlash(baseUrl))
				.build();
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void ensureUserExists(UUID userId) {
		try {
			JsonNode root = restClient.get()
					.uri("/api/v1/users/{userId}/public-profile", userId)
					.accept(MediaType.APPLICATION_JSON)
					.retrieve()
					.body(JsonNode.class);
			if (root == null || !root.path("success").asBoolean(false)) {
				throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage());
			}
		} catch (RestClientResponseException ex) {
			if (ex.getStatusCode().value() == 404) {
				throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage());
			}
			log.warn("Auth user lookup failed: status={}, message={}", ex.getStatusCode(), ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Auth Service is unavailable");
		} catch (RestClientException ex) {
			log.warn("Auth user lookup failed: {}", ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Auth Service is unavailable");
		}
	}

	private String trimTrailingSlash(String baseUrl) {
		if (baseUrl == null || baseUrl.isBlank()) {
			return "http://localhost:3001";
		}
		return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
	}
}
