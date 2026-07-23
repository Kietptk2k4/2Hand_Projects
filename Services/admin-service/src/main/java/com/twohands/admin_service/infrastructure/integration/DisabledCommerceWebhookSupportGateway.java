package com.twohands.admin_service.infrastructure.integration;

import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.integration.CommerceWebhookSupportGateway;
import com.twohands.admin_service.domain.support.WebhookSupportLogEntry;
import com.twohands.admin_service.domain.support.WebhookSupportLogStats;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "admin.integrations.commerce.enabled", havingValue = "false", matchIfMissing = true)
public class DisabledCommerceWebhookSupportGateway implements CommerceWebhookSupportGateway {

	private static final String MESSAGE = "Commerce integration is disabled; webhook logs are unavailable";

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public PagedResult<WebhookSupportLogEntry> searchWebhookLogs(
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
		throw unavailable();
	}

	@Override
	public WebhookSupportLogStats fetchWebhookLogStats(
			String provider,
			String referenceId,
			String searchQuery,
			String eventType,
			String status,
			String from,
			String to,
			String bearerToken
	) {
		throw unavailable();
	}

	@Override
	public WebhookSupportLogEntry fetchWebhookLogDetail(UUID logId, String provider, String bearerToken) {
		throw unavailable();
	}

	@Override
	public byte[] exportWebhookLogsCsv(
			String provider,
			String referenceId,
			String searchQuery,
			String eventType,
			String status,
			String from,
			String to,
			String bearerToken
	) {
		throw unavailable();
	}

	private AppException unavailable() {
		return new AppException(ErrorCode.SERVICE_UNAVAILABLE, MESSAGE);
	}
}
