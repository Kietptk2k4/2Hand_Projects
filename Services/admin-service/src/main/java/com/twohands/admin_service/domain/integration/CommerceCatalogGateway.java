package com.twohands.admin_service.domain.integration;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;
import java.util.UUID;

public interface CommerceCatalogGateway {

    boolean isEnabled();

    JsonNode listCategories(Boolean isActive, String query, String bearerToken);

    JsonNode createCategory(Map<String, Object> body, String bearerToken);

    JsonNode updateCategory(UUID categoryId, Map<String, Object> body, String bearerToken);

    JsonNode setCategoryActive(UUID categoryId, boolean active, String bearerToken);

    JsonNode listBrands(Boolean isActive, String query, Integer page, Integer limit, String bearerToken);

    JsonNode createBrand(Map<String, Object> body, String bearerToken);

    JsonNode updateBrand(UUID brandId, Map<String, Object> body, String bearerToken);

    JsonNode setBrandActive(UUID brandId, boolean active, String bearerToken);
}
