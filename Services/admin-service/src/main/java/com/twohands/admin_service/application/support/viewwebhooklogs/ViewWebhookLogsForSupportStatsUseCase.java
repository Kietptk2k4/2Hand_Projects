package com.twohands.admin_service.application.support.viewwebhooklogs;

import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.integration.CommerceWebhookSupportGateway;
import com.twohands.admin_service.domain.support.WebhookSupportLogStats;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.AdminActionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ViewWebhookLogsForSupportStatsUseCase {

	private static final String SUCCESS_MESSAGE = "Webhook log stats retrieved successfully";

	private final AdminAuthorizationService adminAuthorizationService;
	private final CommerceWebhookSupportGateway commerceWebhookSupportGateway;
	private final AdminActionAuditLogger adminActionAuditLogger;

	public ViewWebhookLogsForSupportStatsUseCase(
			AdminAuthorizationService adminAuthorizationService,
			CommerceWebhookSupportGateway commerceWebhookSupportGateway,
			AdminActionAuditLogger adminActionAuditLogger
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.commerceWebhookSupportGateway = commerceWebhookSupportGateway;
		this.adminActionAuditLogger = adminActionAuditLogger;
	}

	@Transactional
	public WebhookSupportLogStats execute(ViewWebhookLogsForSupportStatsQuery query) {
		UUID adminId = adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.WEBHOOK_SUPPORT_READ);
		ensureCommerceEnabled();

		WebhookSupportLogStats stats = commerceWebhookSupportGateway.fetchWebhookLogStats(
				query.provider(),
				query.referenceId(),
				query.searchQuery(),
				query.eventType(),
				query.status(),
				query.from(),
				query.to(),
				query.bearerToken()
		);

		adminActionAuditLogger.logSuccess(
				adminId,
				AdminActionType.WEBHOOK_SUPPORT_VIEW.name(),
				AdminActionTargetType.WEBHOOK,
				"stats",
				SUCCESS_MESSAGE,
				buildAuditRequest(query),
				Map.of("total", stats.total())
		);

		return stats;
	}

	public String successMessage() {
		return SUCCESS_MESSAGE;
	}

	private void ensureCommerceEnabled() {
		if (!commerceWebhookSupportGateway.isEnabled()) {
			throw new AppException(
					ErrorCode.SERVICE_UNAVAILABLE,
					"Commerce integration is disabled; webhook logs are unavailable"
			);
		}
	}

	private Map<String, Object> buildAuditRequest(ViewWebhookLogsForSupportStatsQuery query) {
		Map<String, Object> request = new HashMap<>();
		if (query.provider() != null && !query.provider().isBlank()) {
			request.put("provider", query.provider());
		}
		return request;
	}
}
