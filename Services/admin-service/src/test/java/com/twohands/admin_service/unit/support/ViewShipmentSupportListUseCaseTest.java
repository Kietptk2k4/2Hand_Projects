package com.twohands.admin_service.unit.support;

import com.twohands.admin_service.application.audit.AdminActionAuditLogger;
import com.twohands.admin_service.application.support.viewshipmentsupportlist.ViewShipmentSupportListQuery;
import com.twohands.admin_service.application.support.viewshipmentsupportlist.ViewShipmentSupportListResult;
import com.twohands.admin_service.application.support.viewshipmentsupportlist.ViewShipmentSupportListUseCase;
import com.twohands.admin_service.constant.AdminPermission;
import com.twohands.admin_service.domain.audit.AdminActionTargetType;
import com.twohands.admin_service.domain.auth.AdminAuthorizationService;
import com.twohands.admin_service.domain.common.PagedResult;
import com.twohands.admin_service.domain.integration.CommerceShipmentSupportGateway;
import com.twohands.admin_service.domain.support.ShipmentSupportListEntry;
import com.twohands.admin_service.exception.AppException;
import com.twohands.admin_service.exception.ErrorCode;
import com.twohands.admin_service.infrastructure.persistence.jpa.enums.AdminActionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ViewShipmentSupportListUseCaseTest {

	private final AdminAuthorizationService adminAuthorizationService = mock(AdminAuthorizationService.class);
	private final CommerceShipmentSupportGateway commerceShipmentSupportGateway =
			mock(CommerceShipmentSupportGateway.class);
	private final AdminActionAuditLogger adminActionAuditLogger = mock(AdminActionAuditLogger.class);

	private ViewShipmentSupportListUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new ViewShipmentSupportListUseCase(
				adminAuthorizationService,
				commerceShipmentSupportGateway,
				adminActionAuditLogger
		);
	}

	@Test
	void execute_returnsPagedShipmentsAndLogsAudit() {
		UUID adminId = UUID.randomUUID();
		ShipmentSupportListEntry entry = new ShipmentSupportListEntry(
				UUID.randomUUID(),
				UUID.randomUUID(),
				UUID.randomUUID(),
				"GHN",
				"SHIPPED",
				"TRACK-1",
				"GHN-1",
				Instant.parse("2026-05-20T08:00:00Z"),
				Instant.parse("2026-05-19T10:00:00Z"),
				Instant.parse("2026-05-20T08:00:00Z")
		);
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(adminId);
		when(commerceShipmentSupportGateway.isEnabled()).thenReturn(true);
		when(commerceShipmentSupportGateway.listShipmentSupport(
				eq("SHIPPED"),
				eq("GHN"),
				eq("updated_at"),
				eq(1),
				eq(20),
				eq("token")
		)).thenReturn(new PagedResult<>(List.of(entry), 1, 20, 1L, 1));

		ViewShipmentSupportListResult result = useCase.execute(new ViewShipmentSupportListQuery(
				"SHIPPED",
				"GHN",
				"updated_at",
				1,
				20,
				"token"
		));

		assertEquals(1, result.totalElements());
		assertEquals("GHN", result.shipments().getFirst().carrier());

		verify(adminAuthorizationService).requirePermission(AdminPermission.SHIPMENT_SUPPORT_READ);
		verify(adminActionAuditLogger).logSuccess(
				eq(adminId),
				eq(AdminActionType.SHIPMENT_SUPPORT_VIEW.name()),
				eq(AdminActionTargetType.SHIPMENT),
				eq("list"),
				any(),
				eq(Map.of("status", "SHIPPED", "carrier", "GHN", "sort", "updated_at", "page", 1, "size", 20)),
				eq(Map.of("totalElements", 1L))
		);
	}

	@Test
	void execute_throwsWhenCommerceIntegrationDisabled() {
		when(adminAuthorizationService.requireCurrentAdminId()).thenReturn(UUID.randomUUID());
		when(commerceShipmentSupportGateway.isEnabled()).thenReturn(false);

		AppException ex = assertThrows(AppException.class, () -> useCase.execute(
				new ViewShipmentSupportListQuery(null, null, null, 1, 20, "token")
		));

		assertEquals(ErrorCode.SERVICE_UNAVAILABLE, ex.getErrorCode());
	}
}
