package com.twohands.admin_service.application.support.vieworderdetail;

import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.integration.CommerceOrderSupportGateway;
import com.twohands.admin_service.domain.support.OrderSupportDetail;
import com.twohands.admin_service.domain.support.OrderSupportDetailPolicy;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.AdminActionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class ViewOrderSupportDetailUseCase {

	private static final String SUCCESS_MESSAGE = "Order support detail retrieved successfully";

	private final AdminAuthorizationService adminAuthorizationService;
	private final CommerceOrderSupportGateway commerceOrderSupportGateway;
	private final AdminActionAuditLogger adminActionAuditLogger;

	public ViewOrderSupportDetailUseCase(
			AdminAuthorizationService adminAuthorizationService,
			CommerceOrderSupportGateway commerceOrderSupportGateway,
			AdminActionAuditLogger adminActionAuditLogger
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.commerceOrderSupportGateway = commerceOrderSupportGateway;
		this.adminActionAuditLogger = adminActionAuditLogger;
	}

	@Transactional
	public ViewOrderSupportDetailResult execute(ViewOrderSupportDetailQuery query) {
		UUID adminId = adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.ORDER_SUPPORT_READ);

		if (!commerceOrderSupportGateway.isEnabled()) {
			throw new AppException(
					ErrorCode.SERVICE_UNAVAILABLE,
					"Commerce integration is disabled; order support detail is unavailable"
			);
		}

		OrderSupportDetail rawDetail = commerceOrderSupportGateway.fetchOrderSupportDetail(
				query.orderId(),
				query.bearerToken()
		);

		boolean revealFullPii = false;
		OrderSupportDetail maskedDetail = OrderSupportDetailPolicy.maskContactFields(rawDetail, revealFullPii);

		adminActionAuditLogger.logSuccess(
				adminId,
				AdminActionType.ORDER_SUPPORT_VIEW.name(),
				AdminActionTargetType.ORDER,
				query.orderId().toString(),
				SUCCESS_MESSAGE,
				Map.of("orderId", query.orderId().toString()),
				Map.of("contactFieldsMasked", !revealFullPii)
		);

		return new ViewOrderSupportDetailResult(maskedDetail, !revealFullPii);
	}

	public String successMessage() {
		return SUCCESS_MESSAGE;
	}
}
