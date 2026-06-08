package com.twohands.commerce_service.delivery.http.admin;

import com.twohands.commerce_service.delivery.http.catalog.PageMetaResponse;

import java.util.List;

public record ViewAdminReviewsForModerationResponse(
        List<AdminReviewListItemResponse> items,
        PageMetaResponse pagination
) {
}
