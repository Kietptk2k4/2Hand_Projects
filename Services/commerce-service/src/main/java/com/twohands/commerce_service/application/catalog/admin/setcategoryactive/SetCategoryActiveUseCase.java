package com.twohands.commerce_service.application.catalog.admin.setcategoryactive;

import com.twohands.commerce_service.domain.catalog.admin.AdminCategoryRow;
import com.twohands.commerce_service.domain.catalog.admin.CatalogAdminRepository;
import com.twohands.commerce_service.exception.AppException;
import com.twohands.commerce_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
public class SetCategoryActiveUseCase {

    private final CatalogAdminRepository catalogAdminRepository;
    private final Clock clock;

    public SetCategoryActiveUseCase(CatalogAdminRepository catalogAdminRepository, Clock clock) {
        this.catalogAdminRepository = catalogAdminRepository;
        this.clock = clock;
    }

    @Transactional
    public AdminCategoryRow execute(SetCategoryActiveCommand command) {
        AdminCategoryRow existing = catalogAdminRepository.findCategoryById(command.categoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        if (!command.active()) {
            if (catalogAdminRepository.hasActiveChildren(command.categoryId())) {
                throw new AppException(ErrorCode.CATALOG_IN_USE, "Category has active child categories");
            }
        }

        Instant now = clock.instant();
        catalogAdminRepository.setCategoryActive(command.categoryId(), command.active(), now);
        return catalogAdminRepository.findCategoryById(command.categoryId())
                .orElseThrow(() -> new AppException(ErrorCode.INTERNAL_ERROR));
    }

    public String successMessage() {
        return "Category status updated successfully";
    }
}
