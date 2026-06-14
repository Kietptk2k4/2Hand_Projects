package com.twohands.admin_service.application.refund.viewrefundapproval;

import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.integration.CommerceRefundSupportGateway;
import com.twohands.admin_service.domain.refund.AdminRefundApprovalItem;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ViewAdminRefundApprovalUseCase {

	private final AdminAuthorizationService adminAuthorizationService;
	private final CommerceRefundSupportGateway commerceRefundSupportGateway;

	public ViewAdminRefundApprovalUseCase(
			AdminAuthorizationService adminAuthorizationService,
			CommerceRefundSupportGateway commerceRefundSupportGateway
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.commerceRefundSupportGateway = commerceRefundSupportGateway;
	}

	@Transactional(readOnly = true)
	public AdminRefundApprovalItem execute(ViewAdminRefundApprovalQuery query) {
		adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.REFUND_SUPPORT_READ);

		if (!commerceRefundSupportGateway.isEnabled()) {
			throw new AppException(
					ErrorCode.SERVICE_UNAVAILABLE,
					"Commerce integration is disabled; refund approvals are unavailable"
			);
		}

		return commerceRefundSupportGateway.getRefundApproval(query.refundRequestId(), query.bearerToken());
	}

	public String successMessage() {
		return "Refund approval detail retrieved successfully";
	}
}
