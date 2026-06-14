package com.twohands.admin_service.application.refund.listrefundapprovals;

import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.integration.CommerceRefundSupportGateway;
import com.twohands.admin_service.domain.refund.AdminRefundApprovalListResult;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.AdminActionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class ListAdminRefundApprovalsUseCase {

	private final AdminAuthorizationService adminAuthorizationService;
	private final CommerceRefundSupportGateway commerceRefundSupportGateway;
	private final AdminActionAuditLogger adminActionAuditLogger;

	public ListAdminRefundApprovalsUseCase(
			AdminAuthorizationService adminAuthorizationService,
			CommerceRefundSupportGateway commerceRefundSupportGateway,
			AdminActionAuditLogger adminActionAuditLogger
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.commerceRefundSupportGateway = commerceRefundSupportGateway;
		this.adminActionAuditLogger = adminActionAuditLogger;
	}

	@Transactional
	public AdminRefundApprovalListResult execute(ListAdminRefundApprovalsQuery query) {
		UUID adminId = adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.REFUND_SUPPORT_READ);

		if (!commerceRefundSupportGateway.isEnabled()) {
			throw new AppException(
					ErrorCode.SERVICE_UNAVAILABLE,
					"Commerce integration is disabled; refund approvals are unavailable"
			);
		}

		AdminRefundApprovalListResult result = commerceRefundSupportGateway.listRefundApprovals(
				query.status(),
				query.page(),
				query.limit(),
				query.bearerToken()
		);

		adminActionAuditLogger.logSuccess(
				adminId,
				AdminActionType.REFUND_SUPPORT_VIEW.name(),
				AdminActionTargetType.REFUND_REQUEST,
				"queue",
				"Refund approval queue viewed",
				Map.of("status", query.status().orElse("ALL"), "page", query.page(), "limit", query.limit()),
				Map.of("totalItems", result.totalItems())
		);

		return result;
	}

	public String successMessage() {
		return "Refund approval queue retrieved successfully";
	}
}
