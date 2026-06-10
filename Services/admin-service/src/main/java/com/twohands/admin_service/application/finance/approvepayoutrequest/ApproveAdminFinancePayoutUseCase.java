package com.twohands.admin_service.application.finance.approvepayoutrequest;



import com.twohands.admin_service.application.audit.AdminActionAuditLogger;

import com.twohands.admin_service.constant.AdminPermission;

import com.twohands.admin_service.domain.audit.AdminActionStatus;

import com.twohands.admin_service.domain.audit.AdminActionTargetType;

import com.twohands.admin_service.domain.auth.AdminAuthorizationService;

import com.twohands.admin_service.domain.integration.CommercePayoutSupportGateway;

import com.twohands.admin_service.domain.payout.AdminPayoutRequestItem;

import com.twohands.admin_service.exception.AppException;

import com.twohands.admin_service.exception.ErrorCode;

import com.twohands.admin_service.infrastructure.persistence.jpa.enums.AdminActionType;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;



import java.util.Map;

import java.util.UUID;



@Service

public class ApproveAdminFinancePayoutUseCase {



	private final AdminAuthorizationService adminAuthorizationService;

	private final CommercePayoutSupportGateway commercePayoutSupportGateway;

	private final AdminActionAuditLogger adminActionAuditLogger;



	public ApproveAdminFinancePayoutUseCase(

			AdminAuthorizationService adminAuthorizationService,

			CommercePayoutSupportGateway commercePayoutSupportGateway,

			AdminActionAuditLogger adminActionAuditLogger

	) {

		this.adminAuthorizationService = adminAuthorizationService;

		this.commercePayoutSupportGateway = commercePayoutSupportGateway;

		this.adminActionAuditLogger = adminActionAuditLogger;

	}



	@Transactional

	public AdminPayoutRequestItem execute(ApproveAdminFinancePayoutCommand command) {

		UUID adminId = adminAuthorizationService.requireCurrentAdminId();

		adminAuthorizationService.requirePermission(AdminPermission.PAYOUT_SUPPORT_APPROVE);



		if (!commercePayoutSupportGateway.isEnabled()) {

			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce integration is disabled");

		}



		AdminPayoutRequestItem result = commercePayoutSupportGateway.approvePayoutRequest(

				command.payoutRequestId(),

				command.bearerToken()

		);



		adminActionAuditLogger.logCritical(

				adminId,

				AdminActionType.PAYOUT_REQUEST_APPROVE.name(),

				AdminActionTargetType.PAYOUT_REQUEST,

				command.payoutRequestId().toString(),

				AdminActionStatus.SUCCESS,

				"Payout request approved",

				"Admin approved payout request",

				Map.of("previousStatus", "REQUESTED"),

				Map.of("currentStatus", result.status(), "amount", result.amount()),

				Map.of(),

				Map.of("sellerId", result.sellerId())

		);



		return result;

	}



	public String successMessage() {

		return "Payout request approved successfully";

	}

}

