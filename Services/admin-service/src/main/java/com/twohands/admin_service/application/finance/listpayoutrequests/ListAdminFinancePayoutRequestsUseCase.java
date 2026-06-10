package com.twohands.admin_service.application.finance.listpayoutrequests;



import com.twohands.admin_service.application.audit.AdminActionAuditLogger;

import com.twohands.admin_service.constant.AdminPermission;

import com.twohands.admin_service.domain.audit.AdminActionTargetType;

import com.twohands.admin_service.domain.auth.AdminAuthorizationService;

import com.twohands.admin_service.domain.integration.CommercePayoutSupportGateway;

import com.twohands.admin_service.domain.payout.AdminPayoutRequestListResult;

import com.twohands.admin_service.exception.AppException;

import com.twohands.admin_service.exception.ErrorCode;

import com.twohands.admin_service.infrastructure.persistence.jpa.enums.AdminActionType;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;



import java.util.Map;

import java.util.UUID;



@Service

public class ListAdminFinancePayoutRequestsUseCase {



	private final AdminAuthorizationService adminAuthorizationService;

	private final CommercePayoutSupportGateway commercePayoutSupportGateway;

	private final AdminActionAuditLogger adminActionAuditLogger;



	public ListAdminFinancePayoutRequestsUseCase(

			AdminAuthorizationService adminAuthorizationService,

			CommercePayoutSupportGateway commercePayoutSupportGateway,

			AdminActionAuditLogger adminActionAuditLogger

	) {

		this.adminAuthorizationService = adminAuthorizationService;

		this.commercePayoutSupportGateway = commercePayoutSupportGateway;

		this.adminActionAuditLogger = adminActionAuditLogger;

	}



	@Transactional

	public AdminPayoutRequestListResult execute(ListAdminFinancePayoutRequestsQuery query) {

		UUID adminId = adminAuthorizationService.requireCurrentAdminId();

		adminAuthorizationService.requirePermission(AdminPermission.PAYOUT_SUPPORT_READ);



		if (!commercePayoutSupportGateway.isEnabled()) {

			throw new AppException(

					ErrorCode.SERVICE_UNAVAILABLE,

					"Commerce integration is disabled; payout queue is unavailable"

			);

		}



		AdminPayoutRequestListResult result = commercePayoutSupportGateway.listPayoutRequests(

				query.status(),

				query.page(),

				query.limit(),

				query.bearerToken()

		);



		adminActionAuditLogger.logSuccess(

				adminId,

				AdminActionType.PAYOUT_SUPPORT_VIEW.name(),

				AdminActionTargetType.PAYOUT_REQUEST,

				"queue",

				"Payout queue viewed",

				Map.of("status", query.status().orElse("ALL"), "page", query.page(), "limit", query.limit()),

				Map.of("totalItems", result.totalItems())

		);



		return result;

	}



	public String successMessage() {

		return "Payout queue retrieved successfully";

	}

}

