package com.twohands.commerce_service.application.catalog.admin.listadminbrands;

import com.twohands.commerce_service.domain.catalog.admin.AdminBrandRow;

import java.util.List;

public record ListAdminBrandsResult(List<AdminBrandRow> items, int page, int limit, long totalItems) {
}
