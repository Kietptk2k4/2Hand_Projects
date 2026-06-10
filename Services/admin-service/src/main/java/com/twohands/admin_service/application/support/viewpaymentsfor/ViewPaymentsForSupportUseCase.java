package com.twohands.admin_service.application.support.viewpaymentsfor;

import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.integration.CommercePaymentSupportGateway;
import com.twohands.admin_service.domain.support.PaymentSupportListEntry;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.AdminActionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ViewPaymentsForSupportUseCase {

	private static final String SUCCESS_MESSAGE = "Payments retrieved successfully";

	private final AdminAuthorizationService adminAuthorizationService;
	private final CommercePaymentSupportGateway commercePaymentSupportGateway;
	private final AdminActionAuditLogger adminActionAuditLogger;

	public ViewPaymentsForSupportUseCase(
			AdminAuthorizationService adminAuthorizationService,
			CommercePaymentSupportGateway commercePaymentSupportGateway,
			AdminActionAuditLogger adminActionAuditLogger
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.commercePaymentSupportGateway = commercePaymentSupportGateway;
		this.adminActionAuditLogger = adminActionAuditLogger;
	}

	@Transactional
	public ViewPaymentsForSupportResult execute(ViewPaymentsForSupportQuery query) {
		UUID adminId = adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.PAYMENT_SUPPORT_READ);

		if (!commercePaymentSupportGateway.isEnabled()) {
			throw new AppException(
					ErrorCode.SERVICE_UNAVAILABLE,
					"Commerce integration is disabled; payment support list is unavailable"
			);
		}

		PagedResult<PaymentSupportListEntry> page = commercePaymentSupportGateway.searchPayments(
				query.status(),
				query.paymentMethod(),
				query.orderId(),
				query.from(),
				query.to(),
				query.page(),
				query.size(),
				query.bearerToken()
		);

		adminActionAuditLogger.logSuccess(
				adminId,
				AdminActionType.PAYMENT_SUPPORT_VIEW.name(),
				AdminActionTargetType.PAYMENT,
				"search",
				SUCCESS_MESSAGE,
				buildAuditRequest(query),
				Map.of("totalElements", page.totalElements())
		);

		return new ViewPaymentsForSupportResult(
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

	private Map<String, Object> buildAuditRequest(ViewPaymentsForSupportQuery query) {
		Map<String, Object> request = new HashMap<>();
		if (query.status() != null && !query.status().isBlank()) {
			request.put("status", query.status());
		}
		if (query.paymentMethod() != null && !query.paymentMethod().isBlank()) {
			request.put("paymentMethod", query.paymentMethod());
		}
		if (query.orderId() != null && !query.orderId().isBlank()) {
			request.put("orderId", query.orderId());
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
