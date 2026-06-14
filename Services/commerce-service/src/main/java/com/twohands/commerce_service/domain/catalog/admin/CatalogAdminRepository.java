package com.twohands.commerce_service.domain.catalog.admin;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CatalogAdminRepository {

    List<AdminCategoryRow> findAllCategories(Boolean activeOnly, String query);

    Optional<AdminCategoryRow> findCategoryById(UUID categoryId);

    boolean existsCategorySlug(String slug, UUID excludeId);

    boolean hasActiveChildren(UUID categoryId);

    long countProductsByCategoryId(UUID categoryId);

    UUID insertCategory(
            UUID id,
            String name,
            String slug,
            UUID parentId,
            int level,
            String path,
            Instant now
    );

    void updateCategory(UUID id, String name, String slug, UUID parentId, int level, String path, Instant now);

    void updateCategorySubtreePaths(String oldPathPrefix, String newPathPrefix, int levelDelta, Instant now);

    void setCategoryActive(UUID id, boolean active, Instant now);

    List<AdminBrandRow> findBrands(Boolean activeOnly, String query, int page, int limit);

    long countBrands(Boolean activeOnly, String query);

    Optional<AdminBrandRow> findBrandById(UUID brandId);

    boolean existsBrandSlug(String slug, UUID excludeId);

    UUID insertBrand(UUID id, String name, String slug, Instant now);

    void updateBrand(UUID id, String name, String slug, Instant now);

    void setBrandActive(UUID id, boolean active, Instant now);
}
