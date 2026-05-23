package com.twohands.admin_service.infrastructure.integration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.twohands.admin_service.domain.integration.AuthUserEnforcementGateway;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;

@Component
@ConditionalOnProperty(name = "admin.integrations.auth.enabled", havingValue = "true")
public class HttpAuthUserEnforcementGateway implements AuthUserEnforcementGateway {

	private static final Logger log = LoggerFactory.getLogger(HttpAuthUserEnforcementGateway.class);

	private final RestClient restClient;

	public HttpAuthUserEnforcementGateway(@Value("${admin.integrations.auth.base-url}") String baseUrl) {
		this.restClient = RestClient.builder()
				.baseUrl(trimTrailingSlash(baseUrl))
				.build();
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void suspendUser(AuthSuspendUserRequest request) {
		postEnforcement(
				"/api/v1/admin/users/{userId}/suspend",
				request.userId(),
				request.enforcementId(),
				request.reasonCode(),
				request.description(),
				request.expiresAt(),
				request.bearerToken(),
				"suspend"
		);
	}

	@Override
	public void banUser(AuthBanUserRequest request) {
		postEnforcement(
				"/api/v1/admin/users/{userId}/ban",
				request.userId(),
				request.enforcementId(),
				request.reasonCode(),
				request.description(),
				request.expiresAt(),
				request.bearerToken(),
				"ban"
		);
	}

	@Override
	public void restrictUser(AuthRestrictUserRequest request) {
		postEnforcement(
				"/api/v1/admin/users/{userId}/restrict",
				request.userId(),
				request.enforcementId(),
				request.reasonCode(),
				request.description(),
				request.expiresAt(),
				request.bearerToken(),
				"restrict"
		);
	}

	private void postEnforcement(
			String uri,
			java.util.UUID userId,
			java.util.UUID enforcementId,
			String reasonCode,
			String description,
			Instant expiresAt,
			String bearerToken,
			String actionLabel
	) {
		try {
			restClient.post()
					.uri(uri, userId)
					.contentType(MediaType.APPLICATION_JSON)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
					.body(new AuthEnforcementBody(enforcementId, reasonCode, description, expiresAt))
					.retrieve()
					.toBodilessEntity();
		} catch (RestClientResponseException ex) {
			if (ex.getStatusCode().is4xxClientError()) {
				JsonNode body = ex.getResponseBodyAs(JsonNode.class);
				throw mapClientError(ex.getStatusCode().value(), body);
			}
			log.warn("Auth {} user failed: status={}, message={}", actionLabel, ex.getStatusCode(), ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Auth Service is unavailable");
		} catch (RestClientException ex) {
			log.warn("Auth {} user failed: {}", actionLabel, ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Auth Service is unavailable");
		}
	}

	private AppException mapClientError(int status, JsonNode body) {
		String message = body == null ? null : body.path("message").asText(null);
		if (message == null || message.isBlank()) {
			message = ErrorCode.RESOURCE_NOT_FOUND.defaultMessage();
		}
		return switch (status) {
			case 404 -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, message);
			case 403 -> new AppException(ErrorCode.FORBIDDEN, message);
			case 409 -> new AppException(ErrorCode.ENFORCEMENT_CONFLICT, message);
			default -> new AppException(ErrorCode.BAD_REQUEST, message);
		};
	}

	private String trimTrailingSlash(String baseUrl) {
		if (baseUrl == null || baseUrl.isBlank()) {
			return "http://localhost:3001";
		}
		return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
	}

	private record AuthEnforcementBody(
			@JsonProperty("enforcement_id")
			java.util.UUID enforcementId,
			@JsonProperty("reason_code")
			String reasonCode,
			String description,
			@JsonProperty("expires_at")
			Instant expiresAt
	) {
	}
}
