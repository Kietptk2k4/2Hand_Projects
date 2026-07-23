package com.twohands.commerce_service.application.admin.viewadminproducts;

public record ViewAdminProductsForModerationCommand(
        Integer page,
        Integer limit,
        String status,
        String searchQuery,
        String sort
) {
}
