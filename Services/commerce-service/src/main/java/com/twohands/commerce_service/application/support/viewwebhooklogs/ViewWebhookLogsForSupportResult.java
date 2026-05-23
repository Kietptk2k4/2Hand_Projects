package com.twohands.commerce_service.application.support.viewwebhooklogs;

import com.twohands.commerce_service.domain.support.WebhookLogSupportEntry;

import java.util.List;

public record ViewWebhookLogsForSupportResult(
        int page,
        int size,
        long totalElements,
        int totalPages,
        List<WebhookLogSupportEntry> logs
) {
}
