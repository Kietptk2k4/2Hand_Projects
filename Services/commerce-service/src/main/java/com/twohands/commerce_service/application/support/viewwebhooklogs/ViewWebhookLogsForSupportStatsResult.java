package com.twohands.commerce_service.application.support.viewwebhooklogs;

import com.twohands.commerce_service.domain.support.WebhookLogSupportStats;

public record ViewWebhookLogsForSupportStatsResult(
        long total,
        long pending,
        long invalidSignature,
        long processed,
        long payosCount,
        long ghnCount
) {
    static ViewWebhookLogsForSupportStatsResult from(WebhookLogSupportStats stats) {
        return new ViewWebhookLogsForSupportStatsResult(
                stats.total(),
                stats.pending(),
                stats.invalidSignature(),
                stats.processed(),
                stats.payosCount(),
                stats.ghnCount()
        );
    }
}
