package com.twohands.commerce_service.application.catalog.viewactivebrands;

import com.twohands.commerce_service.domain.catalog.admin.AdminBrandRow;
import com.twohands.commerce_service.domain.catalog.admin.CatalogAdminRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ViewActiveBrandsUseCase {

    private final CatalogAdminRepository catalogAdminRepository;

    public ViewActiveBrandsUseCase(CatalogAdminRepository catalogAdminRepository) {
        this.catalogAdminRepository = catalogAdminRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminBrandRow> execute() {
        return catalogAdminRepository.findBrands(true, null, 1, 500);
    }

    public String successMessage() {
        return "Brands loaded successfully";
    }
}
