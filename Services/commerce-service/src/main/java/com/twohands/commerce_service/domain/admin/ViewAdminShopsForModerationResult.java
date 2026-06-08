package com.twohands.commerce_service.domain.admin;

import com.twohands.commerce_service.common.pagination.PageMeta;

import java.util.List;

public record ViewAdminShopsForModerationResult(
        List<AdminShopListEntry> items,
        PageMeta pagination
) {
}
