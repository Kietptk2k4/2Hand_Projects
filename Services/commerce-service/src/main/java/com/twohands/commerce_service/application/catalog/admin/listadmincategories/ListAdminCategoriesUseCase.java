package com.twohands.commerce_service.application.catalog.admin.listadmincategories;

import com.twohands.commerce_service.domain.catalog.admin.AdminCategoryRow;
import com.twohands.commerce_service.domain.catalog.admin.CatalogAdminRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListAdminCategoriesUseCase {

    private final CatalogAdminRepository catalogAdminRepository;

    public ListAdminCategoriesUseCase(CatalogAdminRepository catalogAdminRepository) {
        this.catalogAdminRepository = catalogAdminRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminCategoryRow> execute(ListAdminCategoriesCommand command) {
        return catalogAdminRepository.findAllCategories(command.activeOnly(), command.query());
    }

    public String successMessage() {
        return "Categories loaded successfully";
    }
}
