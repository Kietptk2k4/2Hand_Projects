package com.twohands.admin_service.infrastructure.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.twohands.admin_service.domain.auth.AdminCredentialLogin;
import com.twohands.admin_service.domain.auth.AdminLoginTokens;
import com.twohands.admin_service.domain.auth.AuthLoginGateway;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "admin.integrations.auth.enabled", havingValue = "true")
public class HttpAuthLoginGateway implements AuthLoginGateway {

	private static final Logger log = LoggerFactory.getLogger(HttpAuthLoginGateway.class);

	private final RestClient restClient;

	public HttpAuthLoginGateway(@Value("${admin.integrations.auth.base-url}") String baseUrl) {
		this.restClient = RestClient.builder()
				.baseUrl(trimTrailingSlash(baseUrl))
				.build();
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public AdminLoginTokens login(AdminCredentialLogin login) {
		try {
			JsonNode root = restClient.post()
					.uri("/api/v1/auth/admin/login")
					.contentType(MediaType.APPLICATION_JSON)
					.body(new AuthLoginRequestBody(login.email(), login.password()))
					.header("X-Device-Id", login.deviceId())
					.header("User-Agent", login.userAgent())
					.retrieve()
					.body(JsonNode.class);

			return mapSuccess(root);
		} catch (RestClientResponseException ex) {
			if (ex.getStatusCode().is4xxClientError()) {
				JsonNode body = ex.getResponseBodyAs(JsonNode.class);
				throw mapClientError(ex.getStatusCode().value(), body);
			}
			log.warn("Auth admin login call failed: status={}, message={}", ex.getStatusCode(), ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Auth Service is unavailable");
		} catch (AppException ex) {
			throw ex;
		} catch (RestClientException ex) {
			log.warn("Auth admin login call failed: {}", ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Auth Service is unavailable");
		}
	}

	private AdminLoginTokens mapSuccess(JsonNode root) {
		if (root == null || !root.path("success").asBoolean(false)) {
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Invalid response from Auth Service");
		}
		JsonNode data = root.path("data");
		UUID adminId = UUID.fromString(data.path("user").path("id").asText());
		List<String> roles = readStringList(data.path("roles"));
		List<String> permissions = readStringList(data.path("permissions"));
		return new AdminLoginTokens(
				data.path("access_token").asText(),
				data.path("refresh_token").asText(),
				data.path("expires_in").asLong(),
				adminId,
				data.path("user").path("email").asText(),
				data.path("user").path("status").asText(),
				roles,
				permissions
		);
	}

	private AppException mapClientError(int status, JsonNode body) {
		String message = body == null ? null : body.path("message").asText(null);
		if (message == null || message.isBlank()) {
			message = ErrorCode.UNAUTHORIZED.defaultMessage();
		}
		return switch (status) {
			case 403 -> new AppException(ErrorCode.FORBIDDEN, message);
			case 401 -> new AppException(ErrorCode.UNAUTHORIZED, message);
			case 429 -> new AppException(ErrorCode.BAD_REQUEST, message);
			default -> new AppException(ErrorCode.BAD_REQUEST, message);
		};
	}

	private List<String> readStringList(JsonNode arrayNode) {
		if (arrayNode == null || !arrayNode.isArray()) {
			return List.of();
		}
		List<String> values = new ArrayList<>();
		arrayNode.forEach(node -> values.add(node.asText()));
		return values;
	}

	private String trimTrailingSlash(String baseUrl) {
		if (baseUrl == null || baseUrl.isBlank()) {
			return "http://localhost:3001";
		}
		return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
	}

	private record AuthLoginRequestBody(String email, String password) {
	}
}
