package com.twohands.admin_service.application.support.viewshipmentdetail;

import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.integration.CommerceShipmentSupportGateway;
import com.twohands.admin_service.domain.support.ShipmentSupportDetail;
import com.twohands.admin_service.domain.support.ShipmentSupportDetailPolicy;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.AdminActionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class ViewShipmentSupportDetailUseCase {

	private static final String SUCCESS_MESSAGE = "Shipment support detail retrieved successfully";

	private final AdminAuthorizationService adminAuthorizationService;
	private final CommerceShipmentSupportGateway commerceShipmentSupportGateway;
	private final AdminActionAuditLogger adminActionAuditLogger;

	public ViewShipmentSupportDetailUseCase(
			AdminAuthorizationService adminAuthorizationService,
			CommerceShipmentSupportGateway commerceShipmentSupportGateway,
			AdminActionAuditLogger adminActionAuditLogger
	) {
		this.adminAuthorizationService = adminAuthorizationService;
		this.commerceShipmentSupportGateway = commerceShipmentSupportGateway;
		this.adminActionAuditLogger = adminActionAuditLogger;
	}

	@Transactional
	public ViewShipmentSupportDetailResult execute(ViewShipmentSupportDetailQuery query) {
		UUID adminId = adminAuthorizationService.requireCurrentAdminId();
		adminAuthorizationService.requirePermission(AdminPermission.SHIPMENT_SUPPORT_READ);

		if (!commerceShipmentSupportGateway.isEnabled()) {
			throw new AppException(
					ErrorCode.SERVICE_UNAVAILABLE,
					"Commerce integration is disabled; shipment support detail is unavailable"
			);
		}

		ShipmentSupportDetail rawDetail = commerceShipmentSupportGateway.fetchShipmentSupportDetail(
				query.shipmentId(),
				query.bearerToken()
		);

		boolean revealFullPii = false;
		ShipmentSupportDetail maskedDetail = ShipmentSupportDetailPolicy.maskContactFields(rawDetail, revealFullPii);

		adminActionAuditLogger.logSuccess(
				adminId,
				AdminActionType.SHIPMENT_SUPPORT_VIEW.name(),
				AdminActionTargetType.SHIPMENT,
				query.shipmentId().toString(),
				SUCCESS_MESSAGE,
				Map.of("shipmentId", query.shipmentId().toString()),
				Map.of(
						"internalStatus", maskedDetail.internalStatus(),
						"carrierStatus", maskedDetail.carrierStatus() != null ? maskedDetail.carrierStatus() : "",
						"contactFieldsMasked", !revealFullPii
				)
		);

		return new ViewShipmentSupportDetailResult(maskedDetail, !revealFullPii);
	}

	public String successMessage() {
		return SUCCESS_MESSAGE;
	}
}
