package com.twohands.admin_service.application.support.viewwebhooklogs;

import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.integration.CommerceWebhookSupportGateway;
import com.twohands.admin_service.domain.support.WebhookSupportLogEntry;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.AdminActionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ViewWebhookLogsForSupportUseCase {

	private static final String SUCCESS_MESSAGE = "Webhook logs retrieved successfully";

	private final AdminAuthorizationService adminAuthorizationService;
	private final CommerceWebhookSupportGateway commerceWebhookSupportGateway;
	private final AdminActionAuditLogger adminActionAuditLogger;

	public ViewWebhookLogsForSupportUseCase(
			AdminAuthorizationService adminAuthorizationService,
			CommerceWebhookSupportGateway commerceWebhookSupportGateway,
			AdminActionAuditLogger adminActionAuditLogger
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.commerceWebhookSupportGateway = commerceWebhookSupportGateway;
		this.adminActionAuditLogger = adminActionAuditLogger;
	}

	@Transactional
	public ViewWebhookLogsForSupportResult execute(ViewWebhookLogsForSupportQuery query) {
		UUID adminId = adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.WEBHOOK_SUPPORT_READ);

		if (!commerceWebhookSupportGateway.isEnabled()) {
			throw new AppException(
					ErrorCode.SERVICE_UNAVAILABLE,
					"Commerce integration is disabled; webhook logs are unavailable"
			);
		}

		PagedResult<WebhookSupportLogEntry> page = commerceWebhookSupportGateway.searchWebhookLogs(
				query.provider(),
				query.referenceId(),
				query.searchQuery(),
				query.eventType(),
				query.status(),
				query.from(),
				query.to(),
				query.page(),
				query.size(),
				query.bearerToken()
		);

		adminActionAuditLogger.logSuccess(
				adminId,
				AdminActionType.WEBHOOK_SUPPORT_VIEW.name(),
				AdminActionTargetType.WEBHOOK,
				"search",
				SUCCESS_MESSAGE,
				buildAuditRequest(query),
				Map.of("totalElements", page.totalElements())
		);

		return new ViewWebhookLogsForSupportResult(
				page.page(),
				page.size(),
				page.totalElements(),
				page.totalPages(),
				page.items()
		);
	}

	public String successMessage() {
		return SUCCESS_MESSAGE;
	}

	private Map<String, Object> buildAuditRequest(ViewWebhookLogsForSupportQuery query) {
		Map<String, Object> request = new HashMap<>();
		if (query.provider() != null && !query.provider().isBlank()) {
			request.put("provider", query.provider());
		}
		if (query.referenceId() != null && !query.referenceId().isBlank()) {
			request.put("referenceId", query.referenceId());
		}
		if (query.searchQuery() != null && !query.searchQuery().isBlank()) {
			request.put("searchQuery", query.searchQuery());
		}
		if (query.eventType() != null && !query.eventType().isBlank()) {
			request.put("eventType", query.eventType());
		}
		if (query.status() != null && !query.status().isBlank()) {
			request.put("status", query.status());
		}
		if (query.page() != null) {
			request.put("page", query.page());
		}
		if (query.size() != null) {
			request.put("size", query.size());
		}
		return request;
	}
}
