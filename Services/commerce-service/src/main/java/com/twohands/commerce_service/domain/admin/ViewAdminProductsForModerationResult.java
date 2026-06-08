package com.twohands.commerce_service.domain.admin;

import com.twohands.commerce_service.common.pagination.PageMeta;

import java.util.List;

public record ViewAdminProductsForModerationResult(
        List<AdminProductListEntry> items,
        PageMeta pagination
) {
}
