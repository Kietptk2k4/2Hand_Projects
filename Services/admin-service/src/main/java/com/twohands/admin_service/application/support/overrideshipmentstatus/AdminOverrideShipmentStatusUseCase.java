package com.twohands.admin_service.application.support.overrideshipmentstatus;

import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.audit.AdminActionStatus;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.integration.CommerceShipmentSupportGateway;
import com.twohands.admin_service.domain.support.AdminOverrideShipmentStatusResult;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.AdminActionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminOverrideShipmentStatusUseCase {

	private final AdminAuthorizationService adminAuthorizationService;
	private final CommerceShipmentSupportGateway commerceShipmentSupportGateway;
	private final AdminActionAuditLogger adminActionAuditLogger;

	public AdminOverrideShipmentStatusUseCase(
			AdminAuthorizationService adminAuthorizationService,
			CommerceShipmentSupportGateway commerceShipmentSupportGateway,
			AdminActionAuditLogger adminActionAuditLogger
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.commerceShipmentSupportGateway = commerceShipmentSupportGateway;
		this.adminActionAuditLogger = adminActionAuditLogger;
	}

	@Transactional
	public AdminOverrideShipmentStatusResult execute(AdminOverrideShipmentStatusCommand command) {
		UUID adminId = adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.SHIPMENT_SUPPORT_WRITE);
		if (command.force()) {
			adminAuthorizationService.requirePermission(AdminPermission.SHIPMENT_SUPPORT_FORCE_WRITE);
		}

		if (!commerceShipmentSupportGateway.isEnabled()) {
			throw new AppException(
					ErrorCode.SERVICE_UNAVAILABLE,
					"Commerce integration is disabled; shipment status override is unavailable"
			);
		}

		AdminOverrideShipmentStatusResult result = commerceShipmentSupportGateway.overrideShipmentStatus(
				command.shipmentId(),
				command.status(),
				command.reason(),
				command.force(),
				command.bearerToken()
		);

		String message = result.previousStatus().equals(result.currentStatus())
				? "Shipment status unchanged"
				: "Shipment status overridden successfully";

		Map<String, Object> requestSummary = new LinkedHashMap<>();
		requestSummary.put("status", command.status());
		requestSummary.put("reason", command.reason());
		requestSummary.put("force", command.force());

		Map<String, Object> beforeSummary = Map.of("previousStatus", result.previousStatus());
		Map<String, Object> afterSummary = new LinkedHashMap<>();
		afterSummary.put("currentStatus", result.currentStatus());
		afterSummary.put("orderItemsUpdated", result.orderItemsUpdated());
		if (result.rawStatus() != null) {
			afterSummary.put("rawStatus", result.rawStatus());
		}

		adminActionAuditLogger.logCritical(
				adminId,
				AdminActionType.SHIPMENT_STATUS_OVERRIDE.name(),
				AdminActionTargetType.SHIPMENT,
				command.shipmentId().toString(),
				AdminActionStatus.SUCCESS,
				message,
				"Admin override shipment status",
				beforeSummary,
				afterSummary,
				requestSummary,
				Map.of(
						"overrideSource", result.overrideSource() != null ? result.overrideSource() : "",
						"carrier", result.carrier() != null ? result.carrier() : ""
				)
		);

		return result;
	}

	public String successMessage(AdminOverrideShipmentStatusResult result) {
		return result.previousStatus().equals(result.currentStatus())
				? "Shipment status unchanged"
				: "Shipment status overridden successfully";
	}
}
