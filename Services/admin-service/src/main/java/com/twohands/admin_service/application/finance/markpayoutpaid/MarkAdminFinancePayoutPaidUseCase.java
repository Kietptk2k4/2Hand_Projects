package com.twohands.admin_service.application.finance.markpayoutpaid;



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

public class MarkAdminFinancePayoutPaidUseCase {



	private final AdminAuthorizationService adminAuthorizationService;

	private final CommercePayoutSupportGateway commercePayoutSupportGateway;

	private final AdminActionAuditLogger adminActionAuditLogger;



	public MarkAdminFinancePayoutPaidUseCase(

			AdminAuthorizationService adminAuthorizationService,

			CommercePayoutSupportGateway commercePayoutSupportGateway,

			AdminActionAuditLogger adminActionAuditLogger

	) {

		this.adminAuthorizationService = adminAuthorizationService;

		this.commercePayoutSupportGateway = commercePayoutSupportGateway;

		this.adminActionAuditLogger = adminActionAuditLogger;

	}



	@Transactional

	public AdminPayoutRequestItem execute(MarkAdminFinancePayoutPaidCommand command) {

		UUID adminId = adminAuthorizationService.requireCurrentAdminId();

		adminAuthorizationService.requirePermission(AdminPermission.PAYOUT_SUPPORT_APPROVE);



		if (!commercePayoutSupportGateway.isEnabled()) {

			throw new AppException(ErrorCode.SERVICE_UNAVAILABLE, "Commerce integration is disabled");

		}



		AdminPayoutRequestItem result = commercePayoutSupportGateway.markPayoutRequestPaid(

				command.payoutRequestId(),

				command.bankTransferRef(),

				command.bearerToken()

		);



		adminActionAuditLogger.logCritical(

				adminId,

				AdminActionType.PAYOUT_REQUEST_MARK_PAID.name(),

				AdminActionTargetType.PAYOUT_REQUEST,

				command.payoutRequestId().toString(),

				AdminActionStatus.SUCCESS,

				"Payout request marked as paid",

				"Admin marked payout request as paid",

				Map.of("previousStatus", "APPROVED"),

				Map.of("currentStatus", result.status(), "amount", result.amount()),

				Map.of("bankTransferRef", command.bankTransferRef()),

				Map.of("sellerId", result.sellerId())

		);



		return result;

	}



	public String successMessage() {

		return "Payout request marked as paid successfully";

	}

}

