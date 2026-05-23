package com.twohands.commerce_service.domain.support;

import java.util.List;

public record WebhookLogSupportPagedResult(
        List<WebhookLogSupportEntry> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
