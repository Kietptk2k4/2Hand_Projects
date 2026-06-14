package com.twohands.admin_service.infrastructure.integration;

import com.twohands.admin_service.domain.integration.CommerceCatalogGateway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "admin.integrations.commerce.enabled", havingValue = "false", matchIfMissing = true)
public class DisabledCommerceCatalogGateway implements CommerceCatalogGateway {

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public com.fasterxml.jackson.databind.JsonNode listCategories(Boolean isActive, String query, String bearerToken) {
        throw unavailable();
    }

    @Override
    public com.fasterxml.jackson.databind.JsonNode createCategory(Map<String, Object> body, String bearerToken) {
        throw unavailable();
    }

    @Override
    public com.fasterxml.jackson.databind.JsonNode updateCategory(UUID categoryId, Map<String, Object> body, String bearerToken) {
        throw unavailable();
    }

    @Override
    public com.fasterxml.jackson.databind.JsonNode setCategoryActive(UUID categoryId, boolean active, String bearerToken) {
        throw unavailable();
    }

    @Override
    public com.fasterxml.jackson.databind.JsonNode listBrands(Boolean isActive, String query, Integer page, Integer limit, String bearerToken) {
        throw unavailable();
    }

    @Override
    public com.fasterxml.jackson.databind.JsonNode createBrand(Map<String, Object> body, String bearerToken) {
        throw unavailable();
    }

    @Override
    public com.fasterxml.jackson.databind.JsonNode updateBrand(UUID brandId, Map<String, Object> body, String bearerToken) {
        throw unavailable();
    }

    @Override
    public com.fasterxml.jackson.databind.JsonNode setBrandActive(UUID brandId, boolean active, String bearerToken) {
        throw unavailable();
    }

    private UnsupportedOperationException unavailable() {
        return new UnsupportedOperationException("Commerce catalog integration is disabled");
    }
}
