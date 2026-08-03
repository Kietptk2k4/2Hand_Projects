package com.twohands.commerce_service.infrastructure.integration.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.commerce_service.config.AdminIntegrationConfig;
import com.twohands.commerce_service.config.AdminIntegrationProperties;
import com.twohands.commerce_service.domain.integration.AdminSystemConfigClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Optional;

@Component
public class AdminSystemConfigClientAdapter implements AdminSystemConfigClient {

    private static final Logger log = LoggerFactory.getLogger(AdminSystemConfigClientAdapter.class);

    private final RestClient adminRestClient;
    private final AdminIntegrationProperties properties;
    private final ObjectMapper objectMapper;

    public AdminSystemConfigClientAdapter(
            @Qualifier(AdminIntegrationConfig.ADMIN_REST_CLIENT) RestClient adminRestClient,
            AdminIntegrationProperties properties,
            ObjectMapper objectMapper
    ) {
        this.adminRestClient = adminRestClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<String> findActiveConfigValue(String configKey) {
        if (configKey == null || configKey.isBlank() || !properties.isConfigured()) {
            return Optional.empty();
        }
        try {
            RestClient.RequestHeadersUriSpec<?> get = adminRestClient.get();
            RestClient.RequestHeadersSpec<?> spec = get.uri(uriBuilder -> uriBuilder
                    .path("/admin/api/v1/system-configs")
                    .queryParam("q", configKey)
                    .queryParam("is_active", "true")
                    .queryParam("size", 50)
                    .build());
            if (properties.getServiceToken() != null && !properties.getServiceToken().isBlank()) {
                spec = spec.header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getServiceToken());
            }
            String body = spec.retrieve().body(String.class);
            return parseExactValue(body, configKey);
        } catch (RestClientResponseException ex) {
            log.warn(
                    "Admin system-config lookup failed. key={}, status={}",
                    configKey,
                    ex.getStatusCode()
            );
            return Optional.empty();
        } catch (Exception ex) {
            log.warn("Admin system-config lookup failed. key={}, error={}", configKey, ex.getMessage());
            return Optional.empty();
        }
    }

    private Optional<String> parseExactValue(String body, String configKey) {
        if (body == null || body.isBlank()) {
            return Optional.empty();
        }
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode data = root.path("data");
            JsonNode items = data.path("items");
            if (!items.isArray()) {
                items = data.path("content");
            }
            if (!items.isArray() && data.isArray()) {
                items = data;
            }
            if (!items.isArray()) {
                return Optional.empty();
            }
            for (JsonNode item : items) {
                String key = text(item, "configKey");
                if (key == null) {
                    key = text(item, "config_key");
                }
                if (configKey.equals(key)) {
                    String value = text(item, "configValue");
                    if (value == null) {
                        value = text(item, "config_value");
                    }
                    return Optional.ofNullable(value);
                }
            }
            return Optional.empty();
        } catch (Exception ex) {
            log.warn("Failed to parse Admin system-config response for key={}", configKey);
            return Optional.empty();
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        String text = value.asText(null);
        return text != null && !text.isBlank() ? text : null;
    }
}
