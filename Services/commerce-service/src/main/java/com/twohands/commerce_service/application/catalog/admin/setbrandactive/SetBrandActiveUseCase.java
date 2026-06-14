package com.twohands.commerce_service.application.catalog.admin.setbrandactive;

import com.twohands.commerce_service.application.catalog.admin.updatebrand.UpdateBrandUseCase;
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
public class SetBrandActiveUseCase {

    private final CatalogAdminRepository catalogAdminRepository;
    private final Clock clock;

    public SetBrandActiveUseCase(CatalogAdminRepository catalogAdminRepository, Clock clock) {
        this.catalogAdminRepository = catalogAdminRepository;
        this.clock = clock;
    }

    @Transactional
    public AdminBrandRow execute(SetBrandActiveCommand command) {
        AdminBrandRow existing = catalogAdminRepository.findBrandById(command.brandId())
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_FOUND));
        if (UpdateBrandUseCase.PROTECTED_BRAND_SLUG.equalsIgnoreCase(existing.slug())) {
            throw new AppException(ErrorCode.CATALOG_PROTECTED);
        }

        Instant now = clock.instant();
        catalogAdminRepository.setBrandActive(command.brandId(), command.active(), now);
        return catalogAdminRepository.findBrandById(command.brandId())
                .orElseThrow(() -> new AppException(ErrorCode.INTERNAL_ERROR));
    }

    public String successMessage() {
        return "Brand status updated successfully";
    }
}
