package com.twohands.admin_service.infrastructure.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.twohands.admin_service.domain.integration.SocialCommentGateway;
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

import java.util.Optional;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "admin.integrations.social.enabled", havingValue = "true")
public class HttpSocialCommentGateway implements SocialCommentGateway {

	private static final Logger log = LoggerFactory.getLogger(HttpSocialCommentGateway.class);

	private final RestClient restClient;

	public HttpSocialCommentGateway(@Value("${admin.integrations.social.base-url}") String baseUrl) {
		this.restClient = RestClient.builder()
				.baseUrl(trimTrailingSlash(baseUrl))
				.build();
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void ensureCommentExists(String commentId) {
		fetchComment(commentId);
	}

	@Override
	public Optional<UUID> findAuthorUserId(String commentId) {
		JsonNode data = fetchComment(commentId);
		return parseAuthorUserId(data);
	}

	@Override
	public Optional<String> findPostId(String commentId) {
		JsonNode data = fetchComment(commentId);
		String postId = firstNonBlank(
				textValue(data, "post_id"),
				textValue(data, "postId")
		);
		return postId == null ? Optional.empty() : Optional.of(postId);
	}

	private JsonNode fetchComment(String commentId) {
		try {
			JsonNode root = restClient.get()
					.uri("/api/v1/social/comments/{commentId}", commentId)
					.accept(MediaType.APPLICATION_JSON)
					.retrieve()
					.body(JsonNode.class);
			if (root == null || !root.path("success").asBoolean(false)) {
				throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage());
			}
			JsonNode data = root.path("data");
			if (data.isMissingNode() || data.isNull()) {
				throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage());
			}
			return data;
		} catch (RestClientResponseException ex) {
			if (ex.getStatusCode().value() == 404) {
				throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage());
			}
			log.warn("Social comment lookup failed: status={}, message={}", ex.getStatusCode(), ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Social Service is unavailable");
		} catch (RestClientException ex) {
			log.warn("Social comment lookup failed: {}", ex.getMessage());
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Social Service is unavailable");
		}
	}

	private Optional<UUID> parseAuthorUserId(JsonNode data) {
		JsonNode author = data.path("author");
		String authorId = firstNonBlank(
				textValue(author, "userId"),
				textValue(author, "user_id"),
				textValue(data, "authorId"),
				textValue(data, "author_id")
		);
		if (authorId == null) {
			return Optional.empty();
		}
		try {
			return Optional.of(UUID.fromString(authorId));
		} catch (IllegalArgumentException ex) {
			return Optional.empty();
		}
	}

	private String textValue(JsonNode node, String field) {
		if (node == null || node.isMissingNode() || node.isNull()) {
			return null;
		}
		JsonNode valueNode = node.path(field);
		if (valueNode.isMissingNode() || valueNode.isNull() || !valueNode.isValueNode()) {
			return null;
		}
		String value = valueNode.asText();
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}

	private String firstNonBlank(String... values) {
		for (String value : values) {
			if (value != null && !value.isBlank()) {
				return value;
			}
		}
		return null;
	}

	private String trimTrailingSlash(String baseUrl) {
		if (baseUrl == null || baseUrl.isBlank()) {
			return "http://localhost:3002";
		}
		return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
	}
}
