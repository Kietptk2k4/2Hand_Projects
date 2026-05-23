package com.twohands.admin_service.application.support.viewwebhooklogs;

import com.twohands.admin_service.domain.support.WebhookSupportLogEntry;

import java.util.List;

public record ViewWebhookLogsForSupportResult(
		int page,
		int size,
		long totalElements,
		int totalPages,
		List<WebhookSupportLogEntry> logs
) {
}
