package com.twohands.commerce_service.application.catalog.admin.listadminbrands;

public record ListAdminBrandsCommand(Boolean activeOnly, String query, Integer page, Integer limit) {
}
