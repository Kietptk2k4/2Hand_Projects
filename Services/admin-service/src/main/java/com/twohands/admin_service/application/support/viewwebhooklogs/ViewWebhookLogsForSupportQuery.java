package com.twohands.admin_service.application.support.viewwebhooklogs;

public record ViewWebhookLogsForSupportQuery(
		String provider,
		String referenceId,
		String searchQuery,
		String eventType,
		String status,
		String from,
		String to,
		Integer page,
		Integer size,
		String bearerToken
) {
}
