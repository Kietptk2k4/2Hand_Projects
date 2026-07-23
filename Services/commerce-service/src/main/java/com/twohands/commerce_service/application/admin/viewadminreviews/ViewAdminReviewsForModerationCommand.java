package com.twohands.commerce_service.application.admin.viewadminreviews;

public record ViewAdminReviewsForModerationCommand(
        Integer page,
        Integer limit,
        String status,
        Integer rating,
        String searchQuery,
        String sort
) {
}
