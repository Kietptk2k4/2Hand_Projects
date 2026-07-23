package com.twohands.admin_service.application.support.viewwebhooklogs;

public record ViewWebhookLogsForSupportStatsQuery(
		String provider,
		String referenceId,
		String searchQuery,
		String eventType,
		String status,
		String from,
		String to,
		String bearerToken
) {
}
