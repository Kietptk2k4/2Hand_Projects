package com.twohands.admin_service.infrastructure.integration;

import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.integration.CommerceWebhookSupportGateway;
import com.twohands.admin_service.domain.support.WebhookSupportLogEntry;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "admin.integrations.commerce.enabled", havingValue = "false", matchIfMissing = true)
public class DisabledCommerceWebhookSupportGateway implements CommerceWebhookSupportGateway {

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public PagedResult<WebhookSupportLogEntry> searchWebhookLogs(
			String provider,
			String referenceId,
			String status,
			String from,
			String to,
			Integer page,
			Integer size,
			String bearerToken
	) {
		throw new AppException(
				ErrorCode.SERVICE_UNAVAILABLE,
				"Commerce integration is disabled; webhook logs are unavailable"
		);
	}
}
