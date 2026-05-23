package com.twohands.admin_service.infrastructure.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.twohands.admin_service.domain.auth.AdminRefreshTokenRequest;
import com.twohands.admin_service.domain.auth.AdminRefreshedAccessToken;
import com.twohands.admin_service.domain.auth.AuthRefreshGateway;
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
public class HttpAuthRefreshGateway implements AuthRefreshGateway {

	private static final Logger log = LoggerFactory.getLogger(HttpAuthRefreshGateway.class);

	private final RestClient restClient;

	public HttpAuthRefreshGateway(@Value("${admin.integrations.auth.base-url}") String baseUrl) {
		this.restClient = RestClient.builder()
				.baseUrl(trimTrailingSlash(baseUrl))
				.build();
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public AdminRefreshedAccessToken refresh(AdminRefreshTokenRequest request) {
		try {
			JsonNode root = restClient.post()
					.uri("/api/v1/auth/admin/token/refresh")
					.contentType(MediaType.APPLICATION_JSON)
					.body(new AuthRefreshRequestBody(request.refreshToken()))
					.retrieve()
					.body(JsonNode.class);

			return mapSuccess(root);
		} catch (RestClientResponseException ex) {
			if (ex.getStatusCode().is4xxClientError()) {
				JsonNode body = ex.getResponseBodyAs(JsonNode.class);
				throw mapClientError(ex.getStatusCode().value(), body);
			}
			log.warn("Auth admin refresh call failed: status={}, message={}", ex.getStatusCode(), ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Auth Service is unavailable");
		} catch (AppException ex) {
			throw ex;
		} catch (RestClientException ex) {
			log.warn("Auth admin refresh call failed: {}", ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Auth Service is unavailable");
		}
	}

	private AdminRefreshedAccessToken mapSuccess(JsonNode root) {
		if (root == null || !root.path("success").asBoolean(false)) {
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Invalid response from Auth Service");
		}
		JsonNode data = root.path("data");
		UUID adminId = UUID.fromString(data.path("user").path("id").asText());
		return new AdminRefreshedAccessToken(
				data.path("access_token").asText(),
				data.path("expires_in").asLong(),
				adminId,
				data.path("user").path("email").asText(),
				data.path("user").path("status").asText(),
				readStringList(data.path("roles")),
				readStringList(data.path("permissions"))
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

	private record AuthRefreshRequestBody(String refresh_token) {
	}
}
