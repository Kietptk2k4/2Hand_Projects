package com.twohands.social_service.infrastructure.integration.commerce;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twohands.social_service.domain.integration.CommerceProductCatalogClient;
import com.twohands.social_service.domain.integration.CommerceProductSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Optional;

@Component
public class CommerceProductCatalogClientAdapter implements CommerceProductCatalogClient {

    private static final Logger log = LoggerFactory.getLogger(CommerceProductCatalogClientAdapter.class);

    private final RestClient commerceRestClient;
    private final ObjectMapper objectMapper;

    public CommerceProductCatalogClientAdapter(RestClient commerceRestClient, ObjectMapper objectMapper) {
        this.commerceRestClient = commerceRestClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<CommerceProductSnapshot> findVisibleProductSnapshot(String productId) {
        if (productId == null || productId.isBlank()) {
            return Optional.empty();
        }

        try {
            String body = commerceRestClient.get()
                    .uri("/commerce/api/v1/products/{productId}", productId)
                    .retrieve()
                    .body(String.class);

            return parseSuccessBody(body, productId);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            log.warn("Commerce product snapshot lookup failed. productId={}, status={}", productId, ex.getStatusCode());
            return Optional.empty();
        } catch (Exception ex) {
            log.warn("Commerce product snapshot lookup failed. productId={}, error={}", productId, ex.getMessage());
            return Optional.empty();
        }
    }

    private Optional<CommerceProductSnapshot> parseSuccessBody(String body, String productId) {
        if (body == null || body.isBlank()) {
            return Optional.empty();
        }

        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode data = root.path("data");
            if (data.isMissingNode() || data.isNull()) {
                return Optional.empty();
            }

            String title = text(data, "title");
            String categoryName = data.path("category").path("name").asText(null);
            String imageUrl = firstImageUrl(data.path("media"));

            return Optional.of(new CommerceProductSnapshot(
                    productId,
                    title,
                    imageUrl,
                    categoryName
            ));
        } catch (Exception ex) {
            log.warn("Cannot parse commerce product snapshot. productId={}, error={}", productId, ex.getMessage());
            return Optional.empty();
        }
    }

    private String firstImageUrl(JsonNode mediaNode) {
        if (!mediaNode.isArray()) {
            return null;
        }
        for (JsonNode item : mediaNode) {
            if ("IMAGE".equalsIgnoreCase(text(item, "media_type"))) {
                String url = text(item, "media_url");
                if (url != null) {
                    return url;
                }
            }
        }
        if (mediaNode.size() > 0) {
            return text(mediaNode.get(0), "media_url");
        }
        return null;
    }

    private String text(JsonNode node, String field) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return text.isBlank() ? null : text;
    }
}
