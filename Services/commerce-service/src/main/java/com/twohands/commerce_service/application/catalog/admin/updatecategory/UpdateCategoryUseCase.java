package com.twohands.commerce_service.application.catalog.admin.updatecategory;

import com.twohands.commerce_service.application.catalog.common.CatalogSlugNormalizer;
import com.twohands.commerce_service.application.catalog.common.CategoryPathCalculator;
import com.twohands.commerce_service.domain.catalog.admin.AdminCategoryRow;
import com.twohands.commerce_service.domain.catalog.admin.CatalogAdminRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
public class UpdateCategoryUseCase {

    private final CatalogAdminRepository catalogAdminRepository;
    private final Clock clock;

    public UpdateCategoryUseCase(CatalogAdminRepository catalogAdminRepository, Clock clock) {
        this.catalogAdminRepository = catalogAdminRepository;
        this.clock = clock;
    }

    @Transactional
    public AdminCategoryRow execute(UpdateCategoryCommand command) {
        AdminCategoryRow existing = catalogAdminRepository.findCategoryById(command.categoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        String name = requireText(command.name(), "name");
        String slug = resolveSlug(command.slug(), name);
        if (catalogAdminRepository.existsCategorySlug(slug, command.categoryId())) {
            throw new AppException(ErrorCode.CATALOG_SLUG_CONFLICT);
        }

        UUID newParentId = command.parentId();
        if (Objects.equals(command.categoryId(), newParentId)) {
            throw fieldError("parent_id", "cannot be the category itself");
        }

        String parentPath = null;
        if (newParentId != null) {
            AdminCategoryRow parent = catalogAdminRepository.findCategoryById(newParentId)
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            if (!parent.active()) {
                throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
            }
            if (parent.path().startsWith(existing.path())) {
                throw fieldError("parent_id", "cannot move category under its descendant");
            }
            parentPath = parent.path();
        }

        Instant now = clock.instant();
        String newPath = CategoryPathCalculator.buildPath(existing.id(), parentPath);
        int newLevel = CategoryPathCalculator.resolveLevel(parentPath);

        boolean parentChanged = !Objects.equals(existing.parentId(), newParentId);
        if (parentChanged) {
            int levelDelta = newLevel - existing.level();
            catalogAdminRepository.updateCategorySubtreePaths(existing.path(), newPath, levelDelta, now);
        }

        catalogAdminRepository.updateCategory(
                existing.id(),
                name,
                slug,
                newParentId,
                newLevel,
                newPath,
                now
        );

        return catalogAdminRepository.findCategoryById(existing.id())
                .orElseThrow(() -> new AppException(ErrorCode.INTERNAL_ERROR));
    }

    public String successMessage() {
        return "Category updated successfully";
    }

    private String resolveSlug(String slug, String name) {
        String normalized = slug == null || slug.isBlank()
                ? CatalogSlugNormalizer.normalize(name)
                : CatalogSlugNormalizer.normalize(slug);
        if (normalized.isBlank()) {
            throw fieldError("slug", "must not be blank");
        }
        return normalized;
    }

    private String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw fieldError(field, "is required");
        }
        return value.trim();
    }

    private AppException fieldError(String field, String reason) {
        return new AppException(ErrorCode.VALIDATION_ERROR, ErrorCode.VALIDATION_ERROR.defaultMessage(), field, reason);
    }
}
