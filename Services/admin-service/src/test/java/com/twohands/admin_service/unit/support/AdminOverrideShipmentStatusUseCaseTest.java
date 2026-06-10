package com.twohands.admin_service.unit.support;

import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.application.support.overrideshipmentstatus.AdminOverrideShipmentStatusCommand;
import com.twohands.admin_service.application.support.overrideshipmentstatus.AdminOverrideShipmentStatusUseCase;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.audit.AdminActionStatus;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.integration.CommerceShipmentSupportGateway;
import com.twohands.admin_service.domain.support.AdminOverrideShipmentStatusResult;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.AdminActionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminOverrideShipmentStatusUseCaseTest {

	private final AdminAuthorizationService adminAuthorizationService = mock(AdminAuthorizationService.class);
	private final CommerceShipmentSupportGateway commerceShipmentSupportGateway = mock(CommerceShipmentSupportGateway.class);
	private final AdminActionAuditLogger adminActionAuditLogger = mock(AdminActionAuditLogger.class);

	private AdminOverrideShipmentStatusUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new AdminOverrideShipmentStatusUseCase(
				adminAuthorizationService,
				commerceShipmentSupportGateway,
				adminActionAuditLogger
		);
	}

	@Test
	void execute_overridesStatusAndLogsCriticalAudit() {
		UUID adminId = UUID.randomUUID();
		UUID shipmentId = UUID.randomUUID();
		AdminOverrideShipmentStatusResult gatewayResult = sampleResult(shipmentId, "SHIPPED", "DELIVERED");

		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(commerceShipmentSupportGateway.isEnabled()).thenReturn(true);
		when(commerceShipmentSupportGateway.overrideShipmentStatus(
				eq(shipmentId),
				eq("DELIVERED"),
				eq("GHN webhook khong ve sau 48h"),
				eq(false),
				eq("token")
		)).thenReturn(gatewayResult);

		AdminOverrideShipmentStatusResult result = useCase.execute(new AdminOverrideShipmentStatusCommand(
				shipmentId,
				"DELIVERED",
				"GHN webhook khong ve sau 48h",
				false,
				"token"
		));

		assertEquals("DELIVERED", result.currentStatus());
		verify(adminAuthorizationService).requirePermission(AdminPermission.SHIPMENT_SUPPORT_WRITE);
		verify(adminAuthorizationService, never()).requirePermission(AdminPermission.SHIPMENT_SUPPORT_FORCE_WRITE);
		verify(adminActionAuditLogger).logCritical(
				eq(adminId),
				eq(AdminActionType.SHIPMENT_STATUS_OVERRIDE.name()),
				eq(AdminActionTargetType.SHIPMENT),
				eq(shipmentId.toString()),
				eq(AdminActionStatus.SUCCESS),
				eq("Shipment status overridden successfully"),
				eq("Admin override shipment status"),
				eq(Map.of("previousStatus", "SHIPPED")),
				eq(Map.of(
						"currentStatus", "DELIVERED",
						"orderItemsUpdated", 1,
						"rawStatus", "admin_override"
				)),
				eq(Map.of(
						"status", "DELIVERED",
						"reason", "GHN webhook khong ve sau 48h",
						"force", false
				)),
				eq(Map.of("overrideSource", "ADMIN", "carrier", "GHN"))
		);
	}

	@Test
	void execute_requiresForceWritePermissionWhenForceTrue() {
		UUID adminId = UUID.randomUUID();
		UUID shipmentId = UUID.randomUUID();
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(commerceShipmentSupportGateway.isEnabled()).thenReturn(true);
		when(commerceShipmentSupportGateway.overrideShipmentStatus(any(), any(), any(), eq(true), any()))
				.thenReturn(sampleResult(shipmentId, "DELIVERED", "DELIVERED"));

		useCase.execute(new AdminOverrideShipmentStatusCommand(
				shipmentId,
				"DELIVERED",
				"Force override terminal status",
				true,
				"token"
		));

		verify(adminAuthorizationService).requirePermission(AdminPermission.SHIPMENT_SUPPORT_FORCE_WRITE);
	}

	@Test
	void execute_throwsWhenCommerceIntegrationDisabled() {
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(commerceShipmentSupportGateway.isEnabled()).thenReturn(false);

		AppException ex = assertThrows(AppException.class, () -> useCase.execute(
				new AdminOverrideShipmentStatusCommand(
						UUID.randomUUID(),
						"DELIVERED",
						"Reason long enough",
						false,
						"token"
				)
		));

		assertEquals(ErrorCode.SERVICE_UNAVAILABLE, ex.getErrorCode());
	}

	private AdminOverrideShipmentStatusResult sampleResult(UUID shipmentId, String previous, String current) {
		return new AdminOverrideShipmentStatusResult(
				shipmentId,
				UUID.randomUUID(),
				"GHN",
				previous,
				current,
				"ADMIN",
				previous.equals(current) ? null : "admin_override",
				previous.equals(current) ? 0 : 1,
				Instant.parse("2026-06-10T10:00:00Z")
		);
	}
}
