package com.twohands.commerce_service.delivery.http.support;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.commerce_service.application.support.viewwebhooklogs.ViewWebhookLogsForSupportStatsResult;

public record ViewWebhookLogsForSupportStatsResponse(
        long total,
        long pending,
        @JsonProperty("invalid_signature") long invalidSignature,
        long processed,
        @JsonProperty("by_provider") ProviderCounts byProvider
) {
    public static ViewWebhookLogsForSupportStatsResponse from(ViewWebhookLogsForSupportStatsResult result) {
        return new ViewWebhookLogsForSupportStatsResponse(
                result.total(),
                result.pending(),
                result.invalidSignature(),
                result.processed(),
                new ProviderCounts(result.payosCount(), result.ghnCount())
        );
    }

    public record ProviderCounts(
            long payos,
            long ghn
    ) {
    }
}
