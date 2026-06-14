package com.twohands.admin_service.application.refund.rejectrefundapproval;

import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.audit.AdminActionStatus;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.integration.CommerceRefundSupportGateway;
import com.twohands.admin_service.domain.refund.AdminRefundApprovalItem;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.AdminActionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class RejectAdminRefundApprovalUseCase {

	private final AdminAuthorizationService adminAuthorizationService;
	private final CommerceRefundSupportGateway commerceRefundSupportGateway;
	private final AdminActionAuditLogger adminActionAuditLogger;

	public RejectAdminRefundApprovalUseCase(
			AdminAuthorizationService adminAuthorizationService,
			CommerceRefundSupportGateway commerceRefundSupportGateway,
			AdminActionAuditLogger adminActionAuditLogger
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.commerceRefundSupportGateway = commerceRefundSupportGateway;
		this.adminActionAuditLogger = adminActionAuditLogger;
	}

	@Transactional
	public AdminRefundApprovalItem execute(RejectAdminRefundApprovalCommand command) {
		UUID adminId = adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.REFUND_SUPPORT_APPROVE);

		if (!commerceRefundSupportGateway.isEnabled()) {
			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce integration is disabled");
		}

		AdminRefundApprovalItem result = commerceRefundSupportGateway.rejectRefundApproval(
				command.refundRequestId(),
				command.adminNote(),
				command.bearerToken()
		);

		adminActionAuditLogger.logCritical(
				adminId,
				AdminActionType.REFUND_REQUEST_REJECT.name(),
				AdminActionTargetType.REFUND_REQUEST,
				command.refundRequestId().toString(),
				AdminActionStatus.SUCCESS,
				"Refund request rejected",
				"Admin rejected refund request",
				Map.of("previousStatus", "REQUESTED"),
				Map.of("currentStatus", result.status(), "adminNote", command.adminNote()),
				Map.of(),
				Map.of("orderId", result.orderId())
		);

		return result;
	}

	public String successMessage() {
		return "Refund request rejected successfully";
	}
}
