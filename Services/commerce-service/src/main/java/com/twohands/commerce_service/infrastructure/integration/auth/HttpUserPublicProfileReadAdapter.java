package com.twohands.commerce_service.infrastructure.integration.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.twohands.commerce_service.config.CommerceAuthIntegrationProperties;
import com.twohands.commerce_service.domain.integration.UserPublicProfileReadPort;
import com.twohands.commerce_service.domain.integration.UserPublicProfileSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "commerce.integrations.auth.enabled", havingValue = "true", matchIfMissing = true)
public class HttpUserPublicProfileReadAdapter implements UserPublicProfileReadPort {

    private static final Logger log = LoggerFactory.getLogger(HttpUserPublicProfileReadAdapter.class);

    private final RestClient restClient;

    public HttpUserPublicProfileReadAdapter(CommerceAuthIntegrationProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(trimTrailingSlash(properties.getBaseUrl()))
                .build();
    }

    @Override
    public Map<UUID, UserPublicProfileSummary> findByUserIds(Collection<UUID> userIds) {
        Map<UUID, UserPublicProfileSummary> result = new HashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return result;
        }
        for (UUID userId : userIds) {
            if (userId == null || result.containsKey(userId)) {
                continue;
            }
            findOne(userId).ifPresent(profile -> result.put(userId, profile));
        }
        return result;
    }

    private java.util.Optional<UserPublicProfileSummary> findOne(UUID userId) {
        try {
            JsonNode root = restClient.get()
                    .uri("/api/v1/users/{userId}/public-profile", userId)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(JsonNode.class);
            if (root == null || !root.path("success").asBoolean(false)) {
                return java.util.Optional.empty();
            }
            JsonNode data = root.path("data");
            if (data.isMissingNode() || data.isNull()) {
                return java.util.Optional.empty();
            }
            String displayName = textOrNull(data, "display_name", "displayName");
            String avatarUrl = textOrNull(data, "avatar_url", "avatarUrl");
            return java.util.Optional.of(new UserPublicProfileSummary(userId, displayName, avatarUrl));
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() != 404) {
                log.warn("Auth public profile lookup failed for userId={}: status={}", userId, ex.getStatusCode());
            }
            return java.util.Optional.empty();
        } catch (RestClientException ex) {
            log.warn("Auth public profile lookup failed for userId={}: {}", userId, ex.getMessage());
            return java.util.Optional.empty();
        }
    }

    private String textOrNull(JsonNode node, String snakeCase, String camelCase) {
        String value = node.path(snakeCase).asText(null);
        if (value == null || value.isBlank()) {
            value = node.path(camelCase).asText(null);
        }
        return value == null || value.isBlank() ? null : value;
    }

    private String trimTrailingSlash(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "http://localhost:3001";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
