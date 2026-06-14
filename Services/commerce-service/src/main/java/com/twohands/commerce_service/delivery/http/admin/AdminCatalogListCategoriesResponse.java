package com.twohands.commerce_service.delivery.http.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.application.catalog.admin.listadminbrands.ListAdminBrandsResult;

import java.util.List;

public record AdminCatalogListCategoriesResponse(List<AdminCatalogMapper.AdminCategoryResponse> items) {
}

record AdminCatalogListBrandsResponse(
        List<AdminCatalogMapper.AdminBrandResponse> items,
        AdminCatalogBrandPaginationResponse pagination
) {
    static AdminCatalogListBrandsResponse from(ListAdminBrandsResult result) {
        return new AdminCatalogListBrandsResponse(
                result.items().stream().map(AdminCatalogMapper::toBrandResponse).toList(),
                new AdminCatalogBrandPaginationResponse(
                        result.page(),
                        result.limit(),
                        result.totalItems()
                )
        );
    }
}

record AdminCatalogBrandPaginationResponse(
        int page,
        int limit,
        @JsonProperty("total_items") long totalItems
) {
}
