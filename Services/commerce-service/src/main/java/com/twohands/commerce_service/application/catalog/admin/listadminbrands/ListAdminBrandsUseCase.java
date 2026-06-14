package com.twohands.commerce_service.application.catalog.admin.listadminbrands;

import com.twohands.commerce_service.domain.catalog.admin.AdminBrandRow;
import com.twohands.commerce_service.domain.catalog.admin.CatalogAdminRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListAdminBrandsUseCase {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final CatalogAdminRepository catalogAdminRepository;

    public ListAdminBrandsUseCase(CatalogAdminRepository catalogAdminRepository) {
        this.catalogAdminRepository = catalogAdminRepository;
    }

    @Transactional(readOnly = true)
    public ListAdminBrandsResult execute(ListAdminBrandsCommand command) {
        int page = command.page() == null || command.page() < 1 ? DEFAULT_PAGE : command.page();
        int limit = command.limit() == null || command.limit() < 1 ? DEFAULT_LIMIT : Math.min(command.limit(), MAX_LIMIT);
        List<AdminBrandRow> items = catalogAdminRepository.findBrands(command.activeOnly(), command.query(), page, limit);
        long total = catalogAdminRepository.countBrands(command.activeOnly(), command.query());
        return new ListAdminBrandsResult(items, page, limit, total);
    }

    public String successMessage() {
        return "Brands loaded successfully";
    }
}
