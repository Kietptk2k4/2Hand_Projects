package com.twohands.commerce_service.application.catalog.admin.createbrand;

import com.twohands.commerce_service.application.catalog.common.CatalogSlugNormalizer;
import com.twohands.commerce_service.domain.catalog.admin.AdminBrandRow;
import com.twohands.commerce_service.domain.catalog.admin.CatalogAdminRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class CreateBrandUseCase {

    private final CatalogAdminRepository catalogAdminRepository;
    private final Clock clock;

    public CreateBrandUseCase(CatalogAdminRepository catalogAdminRepository, Clock clock) {
        this.catalogAdminRepository = catalogAdminRepository;
        this.clock = clock;
    }

    @Transactional
    public AdminBrandRow execute(CreateBrandCommand command) {
        String name = requireText(command.name(), "name");
        String slug = resolveSlug(command.slug(), name);
        if (catalogAdminRepository.existsBrandSlug(slug, null)) {
            throw new AppException(ErrorCode.CATALOG_SLUG_CONFLICT);
        }

        Instant now = clock.instant();
        UUID id = UUID.randomUUID();
        catalogAdminRepository.insertBrand(id, name, slug, now);
        return catalogAdminRepository.findBrandById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INTERNAL_ERROR));
    }

    public String successMessage() {
        return "Brand created successfully";
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
