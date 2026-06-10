package com.twohands.admin_service.application.support.viewordersfor;

import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.integration.CommerceOrderSupportGateway;
import com.twohands.admin_service.domain.support.OrderSupportListEntry;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.AdminActionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ViewOrdersForSupportUseCase {

	private static final String SUCCESS_MESSAGE = "Orders retrieved successfully";

	private final AdminAuthorizationService adminAuthorizationService;
	private final CommerceOrderSupportGateway commerceOrderSupportGateway;
	private final AdminActionAuditLogger adminActionAuditLogger;

	public ViewOrdersForSupportUseCase(
			AdminAuthorizationService adminAuthorizationService,
			CommerceOrderSupportGateway commerceOrderSupportGateway,
			AdminActionAuditLogger adminActionAuditLogger
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.commerceOrderSupportGateway = commerceOrderSupportGateway;
		this.adminActionAuditLogger = adminActionAuditLogger;
	}

	@Transactional
	public ViewOrdersForSupportResult execute(ViewOrdersForSupportQuery query) {
		UUID adminId = adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.ORDER_SUPPORT_READ);

		if (!commerceOrderSupportGateway.isEnabled()) {
			throw new AppException(
					ErrorCode.SERVICE_UNAVAILABLE,
					"Commerce integration is disabled; order support list is unavailable"
			);
		}

		PagedResult<OrderSupportListEntry> page = commerceOrderSupportGateway.searchOrders(
				query.status(),
				query.paymentMethod(),
				query.from(),
				query.to(),
				query.sort(),
				query.page(),
				query.size(),
				query.bearerToken()
		);

		adminActionAuditLogger.logSuccess(
				adminId,
				AdminActionType.ORDER_SUPPORT_VIEW.name(),
				AdminActionTargetType.ORDER,
				"search",
				SUCCESS_MESSAGE,
				buildAuditRequest(query),
				Map.of("totalElements", page.totalElements())
		);

		return new ViewOrdersForSupportResult(
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

	private Map<String, Object> buildAuditRequest(ViewOrdersForSupportQuery query) {
		Map<String, Object> request = new HashMap<>();
		if (query.status() != null && !query.status().isBlank()) {
			request.put("status", query.status());
		}
		if (query.paymentMethod() != null && !query.paymentMethod().isBlank()) {
			request.put("paymentMethod", query.paymentMethod());
		}
		if (query.from() != null && !query.from().isBlank()) {
			request.put("from", query.from());
		}
		if (query.to() != null && !query.to().isBlank()) {
			request.put("to", query.to());
		}
		if (query.sort() != null && !query.sort().isBlank()) {
			request.put("sort", query.sort());
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
