package com.twohands.admin_service.domain.integration;

import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.support.WebhookSupportLogEntry;
import com.twohands.admin_service.domain.support.WebhookSupportLogStats;

import java.util.UUID;

public interface CommerceWebhookSupportGateway {

	boolean isEnabled();

	PagedResult<WebhookSupportLogEntry> searchWebhookLogs(
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
	);

	WebhookSupportLogStats fetchWebhookLogStats(
			String provider,
			String referenceId,
			String searchQuery,
			String eventType,
			String status,
			String from,
			String to,
			String bearerToken
	);

	WebhookSupportLogEntry fetchWebhookLogDetail(UUID logId, String provider, String bearerToken);

	byte[] exportWebhookLogsCsv(
			String provider,
			String referenceId,
			String searchQuery,
			String eventType,
			String status,
			String from,
			String to,
			String bearerToken
	);
}
