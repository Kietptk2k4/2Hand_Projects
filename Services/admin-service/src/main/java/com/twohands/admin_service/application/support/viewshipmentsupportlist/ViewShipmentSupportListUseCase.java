package com.twohands.admin_service.application.support.viewshipmentsupportlist;

import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.integration.CommerceShipmentSupportGateway;
import com.twohands.admin_service.domain.support.ShipmentSupportListEntry;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.AdminActionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ViewShipmentSupportListUseCase {

	private static final String SUCCESS_MESSAGE = "Shipment support list retrieved successfully";

	private final AdminAuthorizationService adminAuthorizationService;
	private final CommerceShipmentSupportGateway commerceShipmentSupportGateway;
	private final AdminActionAuditLogger adminActionAuditLogger;

	public ViewShipmentSupportListUseCase(
			AdminAuthorizationService adminAuthorizationService,
			CommerceShipmentSupportGateway commerceShipmentSupportGateway,
			AdminActionAuditLogger adminActionAuditLogger
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.commerceShipmentSupportGateway = commerceShipmentSupportGateway;
		this.adminActionAuditLogger = adminActionAuditLogger;
	}

	@Transactional
	public ViewShipmentSupportListResult execute(ViewShipmentSupportListQuery query) {
		UUID adminId = adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.SHIPMENT_SUPPORT_READ);

		if (!commerceShipmentSupportGateway.isEnabled()) {
			throw new AppException(
					ErrorCode.SERVICE_UNAVAILABLE,
					"Commerce integration is disabled; shipment support list is unavailable"
			);
		}

		PagedResult<ShipmentSupportListEntry> page = commerceShipmentSupportGateway.listShipmentSupport(
				query.status(),
				query.carrier(),
				query.sort(),
				query.page(),
				query.size(),
				query.bearerToken()
		);

		adminActionAuditLogger.logSuccess(
				adminId,
				AdminActionType.SHIPMENT_SUPPORT_VIEW.name(),
				AdminActionTargetType.SHIPMENT,
				"list",
				SUCCESS_MESSAGE,
				buildAuditRequest(query),
				Map.of("totalElements", page.totalElements())
		);

		return new ViewShipmentSupportListResult(
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

	private Map<String, Object> buildAuditRequest(ViewShipmentSupportListQuery query) {
		Map<String, Object> request = new HashMap<>();
		if (query.status() != null && !query.status().isBlank()) {
			request.put("status", query.status());
		}
		if (query.carrier() != null && !query.carrier().isBlank()) {
			request.put("carrier", query.carrier());
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
