package com.twohands.admin_service.infrastructure.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.twohands.admin_service.domain.auth.AdminLogoutDelegation;
import com.twohands.admin_service.domain.auth.AuthLogoutGateway;
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

@Component
@ConditionalOnProperty(name = "admin.integrations.auth.enabled", havingValue = "true")
public class HttpAuthLogoutGateway implements AuthLogoutGateway {

	private static final Logger log = LoggerFactory.getLogger(HttpAuthLogoutGateway.class);

	private final RestClient restClient;

	public HttpAuthLogoutGateway(@Value("${admin.integrations.auth.base-url}") String baseUrl) {
		this.restClient = RestClient.builder()
				.baseUrl(trimTrailingSlash(baseUrl))
				.build();
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void logout(AdminLogoutDelegation delegation) {
		try {
			restClient.post()
					.uri("/api/v1/auth/admin/logout")
					.contentType(MediaType.APPLICATION_JSON)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + delegation.bearerToken())
					.body(new AuthLogoutRequestBody(delegation.refreshToken()))
					.retrieve()
					.toBodilessEntity();
		} catch (RestClientResponseException ex) {
			if (ex.getStatusCode().is4xxClientError()) {
				JsonNode body = ex.getResponseBodyAs(JsonNode.class);
				throw mapClientError(ex.getStatusCode().value(), body);
			}
			log.warn("Auth admin logout call failed: status={}, message={}", ex.getStatusCode(), ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Auth Service is unavailable");
		} catch (AppException ex) {
			throw ex;
		} catch (RestClientException ex) {
			log.warn("Auth admin logout call failed: {}", ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Auth Service is unavailable");
		}
	}

	private AppException mapClientError(int status, JsonNode body) {
		String message = body == null ? null : body.path("message").asText(null);
		if (message == null || message.isBlank()) {
			message = ErrorCode.UNAUTHORIZED.defaultMessage();
		}
		return switch (status) {
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

	private record AuthLogoutRequestBody(String refresh_token) {
	}
}
