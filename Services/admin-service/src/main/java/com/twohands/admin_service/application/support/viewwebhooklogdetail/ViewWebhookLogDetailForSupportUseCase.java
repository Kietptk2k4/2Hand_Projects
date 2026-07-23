package com.twohands.admin_service.application.support.viewwebhooklogdetail;

import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.integration.CommerceWebhookSupportGateway;
import com.twohands.admin_service.domain.support.WebhookSupportLogEntry;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.AdminActionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class ViewWebhookLogDetailForSupportUseCase {

	private static final String SUCCESS_MESSAGE = "Webhook log retrieved successfully";

	private final AdminAuthorizationService adminAuthorizationService;
	private final CommerceWebhookSupportGateway commerceWebhookSupportGateway;
	private final AdminActionAuditLogger adminActionAuditLogger;

	public ViewWebhookLogDetailForSupportUseCase(
			AdminAuthorizationService adminAuthorizationService,
			CommerceWebhookSupportGateway commerceWebhookSupportGateway,
			AdminActionAuditLogger adminActionAuditLogger
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.commerceWebhookSupportGateway = commerceWebhookSupportGateway;
		this.adminActionAuditLogger = adminActionAuditLogger;
	}

	@Transactional
	public WebhookSupportLogEntry execute(UUID logId, String provider, String bearerToken) {
		UUID adminId = adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.WEBHOOK_SUPPORT_READ);

		if (!commerceWebhookSupportGateway.isEnabled()) {
			throw new AppException(
					ErrorCode.SERVICE_UNAVAILABLE,
					"Commerce integration is disabled; webhook logs are unavailable"
			);
		}

		WebhookSupportLogEntry entry = commerceWebhookSupportGateway.fetchWebhookLogDetail(logId, provider, bearerToken);

		adminActionAuditLogger.logSuccess(
				adminId,
				AdminActionType.WEBHOOK_SUPPORT_VIEW.name(),
				AdminActionTargetType.WEBHOOK,
				logId.toString(),
				SUCCESS_MESSAGE,
				Map.of("provider", provider),
				Map.of("logId", logId)
		);

		return entry;
	}

	public String successMessage() {
		return SUCCESS_MESSAGE;
	}
}
