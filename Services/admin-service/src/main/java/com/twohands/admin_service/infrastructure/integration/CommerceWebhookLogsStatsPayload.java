package com.twohands.admin_service.infrastructure.integration;

import com.fasterxml.jackson.annotation.JsonProperty;

record CommerceWebhookLogsStatsPayload(
		long total,
		long pending,
		@JsonProperty("invalid_signature") long invalidSignature,
		long processed,
		@JsonProperty("by_provider") ProviderCounts byProvider
) {
	record ProviderCounts(
			long payos,
			long ghn
	) {
	}
}
