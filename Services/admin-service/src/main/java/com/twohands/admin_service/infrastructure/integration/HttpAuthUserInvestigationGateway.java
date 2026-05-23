package com.twohands.admin_service.infrastructure.integration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.admin_service.domain.integration.AuthUserInvestigationGateway;
import com.twohands.admin_service.domain.integration.InvestigationUserProfile;
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
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "admin.integrations.auth.enabled", havingValue = "true")
public class HttpAuthUserInvestigationGateway implements AuthUserInvestigationGateway {

	private static final Logger log = LoggerFactory.getLogger(HttpAuthUserInvestigationGateway.class);

	private final RestClient restClient;

	public HttpAuthUserInvestigationGateway(@Value("${admin.integrations.auth.base-url}") String baseUrl) {
		this.restClient = RestClient.builder()
				.baseUrl(trimTrailingSlash(baseUrl))
				.build();
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public InvestigationUserProfile fetchInvestigationProfile(UUID userId, String bearerToken) {
		try {
			AuthInvestigationProfileResponse body = restClient.get()
					.uri("/api/v1/admin/users/{userId}/investigation-profile", userId)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
					.accept(MediaType.APPLICATION_JSON)
					.retrieve()
					.body(AuthInvestigationProfileResponse.class);

			if (body == null || !body.success() || body.data() == null) {
				throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Auth Service returned an invalid response");
			}
			return toDomain(body.data());
		} catch (RestClientResponseException ex) {
			if (ex.getStatusCode().value() == 404) {
				throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage());
			}
			if (ex.getStatusCode().value() == 403) {
				throw new AppException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.defaultMessage());
			}
			log.warn("Auth investigation profile lookup failed: status={}, message={}", ex.getStatusCode(), ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Auth Service is unavailable");
		} catch (RestClientException ex) {
			log.warn("Auth investigation profile lookup failed: {}", ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Auth Service is unavailable");
		}
	}

	private InvestigationUserProfile toDomain(ProfileData data) {
		return new InvestigationUserProfile(
				UUID.fromString(data.userId()),
				data.email(),
				data.status(),
				data.emailVerified(),
				data.phoneVerified(),
				data.lastLoginAt(),
				data.createdAt(),
				data.displayName(),
				data.avatarUrl(),
				data.bio(),
				data.website(),
				data.isPrivate()
		);
	}

	private String trimTrailingSlash(String baseUrl) {
		if (baseUrl == null || baseUrl.isBlank()) {
			return "http://localhost:3001";
		}
		return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
	}

	private record AuthInvestigationProfileResponse(
			boolean success,
			ProfileData data
	) {
	}

	private record ProfileData(
			@JsonProperty("user_id")
			String userId,
			String email,
			String status,
			@JsonProperty("email_verified")
			boolean emailVerified,
			@JsonProperty("phone_verified")
			boolean phoneVerified,
			@JsonProperty("last_login_at")
			Instant lastLoginAt,
			@JsonProperty("created_at")
			Instant createdAt,
			@JsonProperty("display_name")
			String displayName,
			@JsonProperty("avatar_url")
			String avatarUrl,
			String bio,
			String website,
			@JsonProperty("is_private")
			boolean isPrivate
	) {
	}
}
