package com.twohands.admin_service.infrastructure.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.twohands.admin_service.domain.integration.CommerceCatalogGateway;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "admin.integrations.commerce.enabled", havingValue = "true")
public class HttpCommerceCatalogGateway implements CommerceCatalogGateway {

    private static final Logger log = LoggerFactory.getLogger(HttpCommerceCatalogGateway.class);

    private final RestClient restClient;

    public HttpCommerceCatalogGateway(@Value("${admin.integrations.commerce.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(CommerceIntegrationJsonSupport.trimTrailingSlash(baseUrl))
                .build();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public JsonNode listCategories(Boolean isActive, String query, String bearerToken) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/commerce/api/v1/admin/catalog/categories");
        if (isActive != null) {
            builder.queryParam("is_active", isActive);
        }
        if (query != null && !query.isBlank()) {
            builder.queryParam("q", query);
        }
        return get(builder.build().toUriString(), bearerToken);
    }

    @Override
    public JsonNode createCategory(Map<String, Object> body, String bearerToken) {
        return post("/commerce/api/v1/admin/catalog/categories", body, bearerToken);
    }

    @Override
    public JsonNode updateCategory(UUID categoryId, Map<String, Object> body, String bearerToken) {
        return put("/commerce/api/v1/admin/catalog/categories/" + categoryId, body, bearerToken);
    }

    @Override
    public JsonNode setCategoryActive(UUID categoryId, boolean active, String bearerToken) {
        String action = active ? "activate" : "deactivate";
        return post("/commerce/api/v1/admin/catalog/categories/" + categoryId + "/" + action, Map.of(), bearerToken);
    }

    @Override
    public JsonNode listBrands(Boolean isActive, String query, Integer page, Integer limit, String bearerToken) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/commerce/api/v1/admin/catalog/brands");
        if (isActive != null) {
            builder.queryParam("is_active", isActive);
        }
        if (query != null && !query.isBlank()) {
            builder.queryParam("q", query);
        }
        if (page != null) {
            builder.queryParam("page", page);
        }
        if (limit != null) {
            builder.queryParam("limit", limit);
        }
        return get(builder.build().toUriString(), bearerToken);
    }

    @Override
    public JsonNode createBrand(Map<String, Object> body, String bearerToken) {
        return post("/commerce/api/v1/admin/catalog/brands", body, bearerToken);
    }

    @Override
    public JsonNode updateBrand(UUID brandId, Map<String, Object> body, String bearerToken) {
        return put("/commerce/api/v1/admin/catalog/brands/" + brandId, body, bearerToken);
    }

    @Override
    public JsonNode setBrandActive(UUID brandId, boolean active, String bearerToken) {
        String action = active ? "activate" : "deactivate";
        return post("/commerce/api/v1/admin/catalog/brands/" + brandId + "/" + action, Map.of(), bearerToken);
    }

    private JsonNode get(String uri, String bearerToken) {
        try {
            JsonNode root = restClient.get()
                    .uri(uri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(JsonNode.class);
            CommerceIntegrationJsonSupport.requireSuccess(root);
            return root.path("data");
        } catch (RestClientResponseException ex) {
            throw mapFailure(ex);
        } catch (RestClientException ex) {
            log.warn("Commerce catalog request failed: {}", ex.getMessage());
            throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service is unavailable");
        }
    }

    private JsonNode post(String path, Map<String, Object> body, String bearerToken) {
        try {
            JsonNode root = restClient.post()
                    .uri(path)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            CommerceIntegrationJsonSupport.requireSuccess(root);
            return root.path("data");
        } catch (RestClientResponseException ex) {
            throw mapFailure(ex);
        } catch (RestClientException ex) {
            log.warn("Commerce catalog request failed: {}", ex.getMessage());
            throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service is unavailable");
        }
    }

    private JsonNode put(String path, Map<String, Object> body, String bearerToken) {
        try {
            JsonNode root = restClient.put()
                    .uri(path)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            CommerceIntegrationJsonSupport.requireSuccess(root);
            return root.path("data");
        } catch (RestClientResponseException ex) {
            throw mapFailure(ex);
        } catch (RestClientException ex) {
            log.warn("Commerce catalog request failed: {}", ex.getMessage());
            throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service is unavailable");
        }
    }

    private AppException mapFailure(RestClientResponseException ex) {
        if (ex.getStatusCode().value() == 404) {
            return new AppException(ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND.defaultMessage());
        }
        if (ex.getStatusCode().value() == 403) {
            return new AppException(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.defaultMessage());
        }
        log.warn("Commerce catalog request failed: status={}, message={}", ex.getStatusCode(), ex.getMessage());
        return new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce Service is unavailable");
    }
}
