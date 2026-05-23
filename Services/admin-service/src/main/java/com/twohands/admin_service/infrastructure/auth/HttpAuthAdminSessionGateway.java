package com.twohands.admin_service.infrastructure.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.twohands.admin_service.domain.auth.AdminSessionRevokeRequest;
import com.twohands.admin_service.domain.auth.AdminSessionRevokeResult;
import com.twohands.admin_service.domain.auth.AuthAdminSessionGateway;
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

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "admin.integrations.auth.enabled", havingValue = "true")
public class HttpAuthAdminSessionGateway implements AuthAdminSessionGateway {

	private static final Logger log = LoggerFactory.getLogger(HttpAuthAdminSessionGateway.class);

	private final RestClient restClient;

	public HttpAuthAdminSessionGateway(@Value("${admin.integrations.auth.base-url}") String baseUrl) {
		this.restClient = RestClient.builder()
				.baseUrl(trimTrailingSlash(baseUrl))
				.build();
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public AdminSessionRevokeResult revoke(AdminSessionRevokeRequest request) {
		try {
			JsonNode root = restClient.post()
					.uri("/api/v1/admin/sessions/{sessionId}/revoke", request.sessionId())
					.contentType(MediaType.APPLICATION_JSON)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + request.bearerToken())
					.body(new AuthRevokeRequestBody(request.revokeAllSessions()))
					.retrieve()
					.body(JsonNode.class);

			return mapSuccess(root);
		} catch (RestClientResponseException ex) {
			if (ex.getStatusCode().is4xxClientError()) {
				JsonNode body = ex.getResponseBodyAs(JsonNode.class);
				throw mapClientError(ex.getStatusCode().value(), body);
			}
			log.warn("Auth admin session revoke failed: status={}, message={}", ex.getStatusCode(), ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Auth Service is unavailable");
		} catch (AppException ex) {
			throw ex;
		} catch (RestClientException ex) {
			log.warn("Auth admin session revoke failed: {}", ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Auth Service is unavailable");
		}
	}

	private AdminSessionRevokeResult mapSuccess(JsonNode root) {
		if (root == null || !root.path("success").asBoolean(false)) {
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Invalid response from Auth Service");
		}
		JsonNode data = root.path("data");
		return new AdminSessionRevokeResult(
				UUID.fromString(data.path("target_admin_user_id").asText()),
				UUID.fromString(data.path("session_id").asText()),
				data.path("revoked_session_count").asInt(0),
				data.path("revoke_all_sessions").asBoolean(false)
		);
	}

	private AppException mapClientError(int status, JsonNode body) {
		String message = body == null ? null : body.path("message").asText(null);
		if (message == null || message.isBlank()) {
			message = ErrorCode.INTERNAL_ERROR.defaultMessage();
		}
		return switch (status) {
			case 404 -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, message);
			case 403 -> new AppException(ErrorCode.FORBIDDEN, message);
			case 401 -> new AppException(ErrorCode.UNAUTHORIZED, message);
			default -> new AppException(ErrorCode.BAD_REQUEST, message);
		};
	}

	private String trimTrailingSlash(String baseUrl) {
		if (baseUrl == null || baseUrl.isBlank()) {
			return "http://localhost:3001";
		}
		return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
	}

	private record AuthRevokeRequestBody(
			@JsonProperty("revoke_all_sessions")
			boolean revokeAllSessions
	) {
	}
}
