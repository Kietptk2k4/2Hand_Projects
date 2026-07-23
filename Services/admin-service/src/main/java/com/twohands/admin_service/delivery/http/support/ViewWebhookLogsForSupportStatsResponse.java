package com.twohands.admin_service.delivery.http.support;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twohands.admin_service.domain.support.WebhookSupportLogStats;

public record ViewWebhookLogsForSupportStatsResponse(
		long total,
		long pending,
		@JsonProperty("invalid_signature") long invalidSignature,
		long processed,
		@JsonProperty("by_provider") ProviderCounts byProvider
) {
	public static ViewWebhookLogsForSupportStatsResponse from(WebhookSupportLogStats stats) {
		return new ViewWebhookLogsForSupportStatsResponse(
				stats.total(),
				stats.pending(),
				stats.invalidSignature(),
				stats.processed(),
				new ProviderCounts(stats.payosCount(), stats.ghnCount())
		);
	}

	public record ProviderCounts(
			long payos,
			long ghn
	) {
	}
}
