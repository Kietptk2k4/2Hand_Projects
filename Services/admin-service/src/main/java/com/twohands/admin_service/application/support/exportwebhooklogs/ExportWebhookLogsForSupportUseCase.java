package com.twohands.admin_service.application.support.exportwebhooklogs;

import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.application.support.viewwebhooklogs.ViewWebhookLogsForSupportQuery;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.integration.CommerceWebhookSupportGateway;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.AdminActionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class ExportWebhookLogsForSupportUseCase {

	private static final String SUCCESS_MESSAGE = "Webhook logs exported successfully";

	private final AdminAuthorizationService adminAuthorizationService;
	private final CommerceWebhookSupportGateway commerceWebhookSupportGateway;
	private final AdminActionAuditLogger adminActionAuditLogger;

	public ExportWebhookLogsForSupportUseCase(
			AdminAuthorizationService adminAuthorizationService,
			CommerceWebhookSupportGateway commerceWebhookSupportGateway,
			AdminActionAuditLogger adminActionAuditLogger
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.commerceWebhookSupportGateway = commerceWebhookSupportGateway;
		this.adminActionAuditLogger = adminActionAuditLogger;
	}

	@Transactional
	public byte[] execute(ViewWebhookLogsForSupportQuery query) {
		UUID adminId = adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.WEBHOOK_SUPPORT_READ);

		if (!commerceWebhookSupportGateway.isEnabled()) {
			throw new AppException(
					ErrorCode.SERVICE_UNAVAILABLE,
					"Commerce integration is disabled; webhook logs are unavailable"
			);
		}

		byte[] csv = commerceWebhookSupportGateway.exportWebhookLogsCsv(
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
				"export",
				SUCCESS_MESSAGE,
				Map.of("format", "csv"),
				Map.of("bytes", csv == null ? 0 : csv.length)
		);

		return csv;
	}
}
