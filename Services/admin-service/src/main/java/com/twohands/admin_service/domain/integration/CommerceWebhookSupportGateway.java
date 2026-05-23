package com.twohands.admin_service.domain.integration;

import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.support.WebhookSupportLogEntry;

public interface CommerceWebhookSupportGateway {

	boolean isEnabled();

	PagedResult<WebhookSupportLogEntry> searchWebhookLogs(
			String provider,
			String referenceId,
			String status,
			String from,
			String to,
			Integer page,
			Integer size,
			String bearerToken
	);
}
