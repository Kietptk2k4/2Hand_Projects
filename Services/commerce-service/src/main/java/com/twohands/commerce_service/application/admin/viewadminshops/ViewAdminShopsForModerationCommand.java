package com.twohands.commerce_service.application.admin.viewadminshops;

public record ViewAdminShopsForModerationCommand(
        Integer page,
        Integer limit,
        String status,
        String searchQuery,
        String sort
) {
}
